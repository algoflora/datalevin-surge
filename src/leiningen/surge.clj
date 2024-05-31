(ns leiningen.surge
  (:require [datalevin-surge.core :refer [dispatch-category]]))

(defn surge
  
  "I don't do a lot."

  [_ & args]
  (dispatch-category args))
