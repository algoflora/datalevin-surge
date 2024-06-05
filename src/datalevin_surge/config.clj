(ns datalevin-surge.config
  (:require [datalevin-surge.vars :refer [*project*]]))

(defn migrations-dir
  []
  (or (-> *project* :datalevin-surge :migrations-dir) "./resources/migrations"))
