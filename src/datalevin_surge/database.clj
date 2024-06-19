(ns datalevin-surge.database
  (:require [datalevin.core :as d]
            [tick.core :as t]
            [datalevin-surge.config :as conf]
            [datalevin-surge.profile :refer [profile-uri]]))

(defonce kvs (atom {}))

(defn kv-connection
  [pid]
  (if (contains? @kvs pid)
    (get @kvs pid)
    (let [kv-conn (d/open-kv (profile-uri pid))]
      (swap! kvs #(assoc % pid kv-conn))
      kv-conn)))

(defn open-dbi
  [pid]
  (d/open-dbi (kv-connection pid) conf/dbi-name))

(defn dbi-open?
  [pid]
  (some? (some #{conf/dbi-name} (d/list-dbis (kv-connection pid)))))

(defn write-to-kv
  [pid uuid]
  (when-not (dbi-open? pid)
    (open-dbi pid))
  (d/transact-kv (kv-connection pid) [[:put conf/dbi-name uuid (t/now) :data :data #{:nooverwrite :nodupdata}]]))

(defn remove-from-kv
  [pid uuid]
  (when-not (dbi-open? pid)
    (open-dbi pid))
  (d/transact-kv (kv-connection pid) [[:del conf/dbi-name uuid]]))

(defn read-kv
  [pid]
  (d/open-dbi (kv-connection pid) conf/dbi-name)
  (d/get-range (kv-connection pid) conf/dbi-name [:all] :data :data))

(defn remote-connection
  [pid]
  (d/get-conn (profile-uri pid)))

(defn remote-schema
  [pid]
  (d/schema (remote-connection pid)))

(defn use!
  [pid {:keys [fname muuid mdata]}]
  (declare conn) ; For .clj-kondo calmness...
  (d/with-transaction [conn (remote-connection pid)]
    (try
      (let [stage-fn (some-> mdata :stage-fn)
            unstage-fn (some-> mdata :unstage-fn)
            stage (apply (eval stage-fn) [conn])]
        (doseq [del-attr (:schema-remove mdata)]
          (let [es (d/q '[:find [?e ...]
                          :in $ ?del-attr
                          :where [?e ?del-attr]] @conn del-attr)]
            (d/transact! conn (mapv #(vector :db/retract % del-attr) es))))
        (d/update-schema conn (:schema-insert mdata) (:schema-remove mdata))
        (apply (eval unstage-fn) [conn stage]))
      (catch Exception ex
        (d/abort-transact conn)
        (println (format "[ERROR]\tBad Migration %s!\n" fname))
        (throw ex))))
  (write-to-kv pid muuid)
  (println (format "[OK]\tMigration %s successfully applied" fname)))
