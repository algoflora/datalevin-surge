(ns datalevin-surge.init
  (:require [clojure.java.io :as io]
            [datalevin-surge.config :as conf]
            [datalevin-surge.clear :as clear]
            [datalevin-surge.databases :as db]
            [datalevin-surge.profile :as prof]))

(defn- check-initialization
  []
  (println "Checking initialization...")
  (try
    (let [db (io/file conf/database-dir)
          fs (io/file conf/migrations-dir)]
      (and (.exists db)
           (.exists fs)
           (.isDirectory db)
           (.isDirectory fs)))
    (catch Exception _
      false)))

(defn- ask-profile-id!
  []
  (loop []
    (print "Enter initial profile id [:dev]: ")
    (let [in (read-line)]
      (if (empty? in)
        :dev
        (if-let [result (some->> in
                                 (re-find #"[^a-z0-9_]*([a-z0-9_]+).*")
                                 second
                                 keyword)]
          result
          (do (println "Wrong input!")
              (recur)))))))

(defn- ask-profile-uri!
  []
  (loop []
    (print "Enter connection URI (folder or remote): ")
    (let [in (read-line)
          f  (io/file in)]
      (if (and (.exists f)
               (.isDirectory f))
        in
        (let [uri (java.net.URI. in)]
          (if (and (some? (.getHost uri))
                   (some? (.getPath uri))
                   (= "dtlv" (.getScheme uri)))
            in
            (do (println "Wrong input!")
                (recur))))))))

(defn- ask-approve!
  [pid puri]
  (print (format "Do you want to initialize dtlv-surge in current folder with initial profile '%s' for database '%s'? (Y/n): " pid puri))
  (loop [in (read-line)]  
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur (read-line)))))

(defn- ask-options
  []
  (loop []
    (let [pid  (ask-profile-id!)
          puri (ask-profile-uri!)]
      (if (true? ask-approve!)
        [pid puri]
        (recur)))))

(defn- initialize
  []
  (let [[pid puri] (ask-options)]
    (clear/main true)
    (db/surge-connection)
    (prof/new pid puri)
    (println (format "Created profile %s with connection URI %s" pid puri))
    ))

(defn main
  []
  (if (check-initialization)
    (println "Looks like Datalevin Surge is already initialized in this folder. Use 'surge check' to check consistency or 'surge clear' to clear all Datalevin Surge data.")
    (initialize)))
