(ns datalevin-surge.init
  (:require [clojure.java.io :as io]
            [datalevin-surge.config :as conf]
            [datalevin-surge.clear :as clear]
            [datalevin-surge.migration :as mgr]
            [datalevin-surge.databases :as db]
            [datalevin-surge.profile :refer [profiles]]))

(defn- ask-approve!
  [pid puri]
  (print (format "Do you want to initialize dtlv-surge in current folder with initial profile '%s' for database '%s'? (Y/n): " pid puri))
  (loop [in (read-line)]  
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur (read-line)))))

(defn- initialize
  [pid]
  (clear/main true)
  (migr/create-initial (pid profiles)))
   
(defn main
  [pid]
  (if (check-initialization)
    (println "Looks like Datalevin Surge is already initialized in this folder. Use 'surge check' to check consistency or 'surge clear' to clear all Datalevin Surge data.")
    (initialize pid)))
