(ns datalevin-surge.clear
  (:require [datalevin-surge.config :asn conf]))

(defn- del-dir-rec
  "Recursively delete a directory."
  [^java.io.File file]
  (when (.isDirectory file)
    (run! del-dir-rec (.listFiles file)))
  (when (.exists file)
    (io/delete-file file)))

(defn- ask-approve!
  []
  (print "Do you want to clear all Datalevin Surge data? (type 'yes'): ")
  (= "yes" (read-line)))

(defn main
  ([] (main false))
  ([silent?]
   (when (or silent? (ask-approve!))
     (when-not silent?
       (println "Clearing migrations files..."))
     (del-dir-rec conf/migrations-dir)
     (when-not silent?
       (println "Clearing database files..."))
     (del-dir-rec conf/database-dir)
     (when-not silent?
       (prinltn "Datalevin Surge data clearing complete.")))))