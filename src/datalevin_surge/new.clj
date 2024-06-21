(ns datalevin-surge.new
  (:require [datalevin-surge.migration :as migr]))

(defn process
  ([]
   (print "Enter new migration name: ")
   (process (read-line)))
  ([name]
   (let [parent (last (migr/sorted-local-migrations))
         num    (->> parent
                     :filename
                     (re-matches #"^(\d{4})(\.\d+)?-.*$")
                     second
                     Integer/parseInt
                     inc)]
     (migr/create-new num (:uuid parent) name))))
