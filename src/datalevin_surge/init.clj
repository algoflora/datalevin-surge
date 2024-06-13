(ns datalevin-surge.init
  (:require [datalevin-surge.check :as check]
            [datalevin-surge.migration :as mgr]
            [datalevin-surge.database :as db]
            [datalevin-surge.profile :refer [profile-uri]]))

(defn- ask-approve!
  [prompt]
  (print prompt)
  (loop [in (read-line)]
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur (read-line)))))

(defn- init-remote
  [pid]
  (db/open-dbi pid))

(defn- init-local
  [pid]
  (let [uuid (mgr/create-initial (profile-uri pid))]
    (db/write-to-kv pid uuid)))

(defn process
  [pid]
  (let [remote-init? (check/remote-init? pid)
        local-init?  (check/local-init?)]
    (cond
      (and (:ok remote-init?) (:ok local-init?))
      (println (format "Looks like Datalevin Surge is already sucessfully initialized in this folder and '%s' database. Use 'surge check' to check consistency." (profile-uri pid)))

      (and (not (:ok remote-init?)) (:ok local-init?))
      (when (ask-approve! (format "Do you want to initialize Datalevin Surge for database '%s'? (y/n): " (profile-uri pid)))
        (init-remote pid))

      (and (:ok remote-init?) (not (:ok local-init?)))
      (println "[ERROR] Something wrong! Use 'surge check' to check!")
      
      :else
      (when (ask-approve! (format "Do you want to initialise Datalevin Surge migration tool in current folder and database '%s'? (y/n): " (profile-uri pid)))
        (init-remote pid)
        (init-local pid)))))

(defn main
  [pid]
  (process pid))
