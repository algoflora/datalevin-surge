(ns datlevin-surge.profile
  (:require [datalevin.core :as d]
            [datalevin-surge.databases :as db]))

(defn new
  [id uri]
  {:pre [(keyword? id)
         (string? uri)]}
  (d/transact! (db/migrations-conn) [{:profile/id  id
                                      :profile/uri uri}])
  (println (format "Created profile %s with connection URI %s" id uri)))
