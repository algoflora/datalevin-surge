(ns datalevin-surge.migration
  (:require [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
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
    (println (format "\"%s\" migration created in file %s" initial-migration-name path))
    uuid))

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

(defn- up
  [pid]
  (let [local  (sorted-local-migrations)
        remote (->> (db/read-kv pid) (into []) (sort-by second))]
    (cond
      (= (count local) (count remote))
      (println "All up migrations are completed!")

      :else (doseq [mig (drop (count remote) local)]
              (db/use! pid {:up mig})))))

(defn process
  [pid direction]
  (case direction
    :up (up pid)
    :down nil #_(down pid)
    :else (throw (ex-info "Wrong direction of migration!" {:direction direction}))))
