(ns datalevin-surge.migration
  (:require [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [comb.template :as template]
            [clojure.java.io :as io]))

(def ^:private initial-migration-name "Initial migration")

(defn- pprn-str
  [obj]
  (with-out-str (pprint obj)))

(defn- format-date-
  [date pattern]
  (let [formatter (java.text.SimpleDateFormat. pattern)]
    (.format formatter date)))

(defn- format-date
  [date]
  (format-date- date "yyyy-MM-dd HH:mm:ss"))

(defn- date-str
  []
  (format-date (java.util.Date.)))

(defn- create-filename
  [num name]
  (as-> name $
      (str/lower-case $)
      (str/replace $ #" " "-")
      (format "%0,4d-%s.edn" num $)))

(defn- compose-initial
  [uri uuid]
  (let [schema (-> uri db/remote-schema pprn-str)
        pname  (:name *project*)
        time   (date-str)]
    (template/eval (io/resource "init-migration.comb") {:migration-name
                                                        initial-migration-name
                                                        :project-name   pname
                                                        :created-on     time
                                                        :uuid           uuid
                                                        :schema         schema})))

(defn create-initial
  [uri]
  (let [filename (create-filename 0 initial-migration-name)
        path     (format "%s/%s" conf/migrations-dir filename)
        uuid     (java.util.UUID/randomUUID)]
    (spit path (compose-initial uri uuid))
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
       (map #(-> % slurp read-string (assoc :filename (-> % .toPath .getFileName str))))))

;;; TODO: Use delay!!!
(defn sorted-local-migrations
  []
  (reverse (sort-migrations (raw-local-migrations))))

