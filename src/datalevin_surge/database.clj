(ns datalevin-surge.database
  (:require [datalevin.core :as d]
            [clojure.java.io :as io]
            [datalevin-surge.profile :as prof]))

(defn remote-connection
  [pid]
  (d/get-conn (prof/uri pid)))

(def internal-schema
  (-> "schema.edn"
      io/resource
      slurp
      read-string))

(defn raw-remote-schema
  [pid]
  (d/schema (remote-connection pid)))

(defn remote-schema
  [pid]
  (apply dissoc (raw-remote-schema pid) (keys internal-schema)))
