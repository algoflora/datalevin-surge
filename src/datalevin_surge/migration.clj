(ns datalevin-surge.migration
  (:require [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
            [datalevin-surge.misc :as misc]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [comb.template :as template]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [tick.core :as t]))

(def ^:private initial-migration-name "Initial migration")

(defn- pprn-str
  [obj]
  (with-out-str (pprint obj)))

(defn- format-date-
  [date pattern]
  (let [formatter (t/formatter pattern)]
    (t/format formatter (t/zoned-date-time date))))

(defn- format-date
  [date]
  (format-date- date "yyyy-MM-dd HH:mm:ss"))

(defn- date-str
  []
  (format-date (t/now)))

(defn- create-filename
  [num name]
  (as-> name $
      (str/lower-case $)
      (str/replace $ #" " "-")
      (format "%0,4d-%s.edn" num $)))

(defn- compose-initial
  [pid uuid]
  (let [schema   (into {} (map (fn [[k v]] [k (dissoc v :db/aid)])) (dissoc (db/remote-schema pid) :db/created-at :db/ident :db/updated-at))
        pname    (:name *project*)
        time     (date-str)
        bindings {:migration-name
                  initial-migration-name
                  :project-name   pname
                  :created-on     time
                  :uuid           uuid
                  :schema         (pprn-str schema)
                  :attributes     (set (keys schema))}]
    (template/eval (io/resource "init-migration.comb") bindings)))

(defn create-initial
  [pid]
  (let [filename (create-filename 0 initial-migration-name)
        path     (format "%s/%s" (conf/migrations-dir) filename)
        uuid     (random-uuid)]
    (spit path (compose-initial pid uuid))
    (println (format "[OK]\t\"%s\" migration created in file %s" initial-migration-name path))
    uuid))

(defn- compose-new
  [uuid parent name]
  (let [bindings {:migration-name name
                  :project-name   (:name *project*)
                  :created-on     (date-str)
                  :uuid           uuid
                  :parent         parent}]
    (template/eval (io/resource "new-migration.comb") bindings)))

(defn create-new
  [num parent name]
  (let [filename (create-filename num name)
        path     (format "%s/%s" (conf/migrations-dir) filename)]
    (spit path (compose-new (random-uuid) parent name))
    (println (format "[OK]\t\"%s\" migration created in file %s" name path))))

(defn sort-migrations
  [migrations]
  (if (empty? migrations)
    migrations
    (loop [ms  migrations
           acc '()]
      (if (empty? acc)
        (let [inits (filter #(and (contains? % :parent) (nil? (:parent %))) ms)]
          (if (= 1 (count inits))
            (let [init (first inits)]
              (recur (filter #(not= init %) ms)
                     (conj acc init)))
            (throw (ex-info "Wrong count of initial migration files!"
                            {:count (count inits)
                             :files (map :filename inits)}))))
        (let [last  (first acc)
              nexts (filter #(= (:parent %) (:uuid last)) ms)
              cnt   (count nexts)]
          (cond (= 0 cnt) acc
                (< 1 cnt) (throw (ex-info "Several migration files with same :parent!"
                                          {:count (count nexts)
                                           :files (map :filename nexts)}))
                :else (let [next (first nexts)]
                        (recur (filter #(not= next %) ms)
                               (conj acc next)))))))))

(defn raw-local-migrations
  []
  (->> (conf/migrations-dir)
       io/file
       file-seq
       (filter #(-> % .toPath .getFileName str (str/ends-with? ".edn")))
       (filter #(-> % .toPath .toFile .isFile))
       (map #(-> % io/reader java.io.PushbackReader. edn/read (assoc :filename (-> % .toPath .getFileName str))))))

;;; TODO: Use delay!!!
(defn sorted-local-migrations
  []
  (reverse (sort-migrations (raw-local-migrations))))

(defn up
  [pid num]
  (let [local  (sorted-local-migrations)
        remote (->> (db/read-kv pid) (into []) (sort-by second))]
    (cond
      (= (count local) (count remote))
      (println "All up migrations are applied!")

      :else
      (let [migs (take (if (= :all num) Long/MAX_VALUE num) (drop (count remote) local))]
        (when (misc/ask-approve! (format "Do you want to apply %d migration(s)? (y/n) " (count migs)))
          (try
            (doseq [mig migs]
              (db/use! pid {:fname (:filename mig)
                            :mdata (:up mig)})
              (db/write-to-kv pid (:uuid mig))
              (println (format "[OK]\tMigration %s successfully applied" (:filename mig))))
            (catch Exception ex
              (println (.getMessage ex)))))))))

(defn down
  [pid num]
  (let [local  (sorted-local-migrations)
        remote (->> (db/read-kv pid) (into []) (sort-by second))]
    (cond
      (zero? (count remote))
      (println "No migrations to revert!")

      :else
      (let [migs (take (if (= :all num) Long/MAX_VALUE num) (reverse (take (count remote) local)))]
        (when (misc/ask-approve! (format "Do you want to revert %d migration(s)? (y/n) " (count migs)))
          (try
            (doseq [mig migs]
              (db/use! pid {:fname (:filename mig)
                            :mdata (:down mig)})
              (db/remove-from-kv pid (:uuid mig))
              (println (format "[OK]\tMigration %s successfully reverted" (:filename mig))))
            (catch Exception ex
              (println (.getMessage ex)))))))))
