(ns datalevin-surge.config
  (:require [clojure.java.io :as io]))

(def database-dir "./.dtlv-surge")

(def database-schema (->> "schema.edn" io/resource slurp read-string (reduce merge {})))

(def migrations-dir "./dtlv-surge")
