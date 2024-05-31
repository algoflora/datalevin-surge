(ns datalevin-surge.profile
  (:require [datalevin.core :as d]
            [datalevin-surge.databases :as db]))

(defn new
  [id uri]
  {:pre [(keyword? id)
         (string? uri)]}
  (d/transact! (db/surge-connection) [{:profile/id  id :profile/uri uri}]))

(defn fetch
  [id]
  (d/pull '[*] [:profile/id id]))

(defn delete
  [id]
  (d/transact! (db/surge-connection) [[:db/retractEntity [:profile/id id]]]))

(defn fetch-all
  []
  (d/q '[:find (pull ?p [*])
         :where [?p :profile/id]] (d/db (db/surge-connection))))
