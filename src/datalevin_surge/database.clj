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
  (d/open-dbi (kv-connection pid) conf/dbi-name)
  (d/transact-kv (kv-connection pid) [[:put conf/dbi-name uuid (t/now) :uuid :instant #{:nooverwrite}]]))

(defn read-kv
  [pid]
  (d/open-dbi (kv-connection pid) conf/dbi-name)
  (d/get-range (kv-connection pid) conf/dbi-name [:all] :uuid :instant))

(defn remote-connection
  [pid]
  (d/get-conn (profile-uri pid)))

(defn remote-schema
  [pid]
  (d/schema (remote-connection pid)))
