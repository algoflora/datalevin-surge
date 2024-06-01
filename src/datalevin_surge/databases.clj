(ns datalevin-surge.databases
  (:require [datalevin.core :as d]
            [datalevin-surge.config :as conf]
            [datalevin-surge.profile :as prof]))

(def ^:private options
  {:validate-data? true
   :closed-schema? true})

(defn surge-connection
  []
  (d/get-conn conf/database-dir conf/database-schema options))

(defn target-connection
  [pid]
  (let [uri (-> pid prof/fetch :profile/uri)]
    (d/get-conn uri)))

(defn target-schema
  [pid]
  (d/schema (target-connection pid)))
