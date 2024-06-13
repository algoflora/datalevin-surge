(ns datalevin-surge.config
  (:require [datalevin-surge.vars :as vars]))

(defn migrations-dir
  []
  (or (-> vars/*project* :datalevin-surge :migrations-dir) "./resources/migrations"))

(def dbi-name "datalevin-surge")
