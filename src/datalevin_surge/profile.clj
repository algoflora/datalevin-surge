(ns datalevin-surge.profile
  (:require [datalevin-surge.vars :refer [*project*]]))

(defn profile-uri
  [id]
  (let [p (-> *project* :datalevin-surge :profiles id)]
    (if (map? p) (:uri p) p)))
