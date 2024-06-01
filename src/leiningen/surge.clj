(ns leiningen.surge
  (:require [datalevin-surge.core :refer [dispatch-category]]
            [datalevin-surge.vars :refer [*project*]]))

(defn surge
  
  "I don't do a lot."

  [project & args]
  (binding [*project* project]
    (dispatch-category args)))
