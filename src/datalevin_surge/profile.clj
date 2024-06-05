(ns datalevin-surge.profile
  (:require [datalevin-surge.vars :refer [*project*]]))

(defn uri
  [id]
  (-> *project* :datalevin-surge :profiles id
      #(if (map? %) (:uri %) %)))
