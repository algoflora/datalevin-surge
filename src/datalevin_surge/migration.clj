(ns datalevin-surge.migration
  (:require [datalevin-surge.databases :as db]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [comb.template :as template]
            [clojure.java.io :as io]))

(def initial-migration-name "Initial migration")

(declare migrations)

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
  (format-date (java.utils.Date.)))

(defn- create-filename
  [num name]
  (as-> name $
      (str/lower-case $)
      (str/replace $ #" " "-")
      (format "%0,4d-%s.edn" num $)))

(defn- compose-initial
  [uri]
  (let [schema (-> uri db/target-schema pprn-str)
        pname  (:name *project*)
        time   (date-str)
        uuid   (java.util.UUID/randomUUID)]
    (template/eval (io/resource "init-migration.comb") {:migration-name
                                                        initial-migration-name
                                                        :project-name   pname
                                                        :created-on     time
                                                        :uuid           uuid
                                                        :schema         schema})))

(defn create-initial
  [uri]
  (let [filename (create-filename 0 initial-migration-name)
        path     (format "%s/%s" conf/migrations-dir filename)]
    (spit path (compose-initial uri))
    (println (format "\"%s\" migration created in file %s" initial-migration-name path))))

