(ns leiningen.dtlv-surge.init
  (:require [datalevin.core :as d]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [leiningen.dtlv-surge.confis :as conf]))

(defn- check-initialization
  []
  (println "Checking initialization...")
  (try
    (let [db (io/file conf/database-dir)
          fs (io/file conf/migrations-dir)]
      (and (.exists db)
           (.exists fs)
           (.isDirectory db)
           (.isDirectory fs))
      (catch Exception _
        false))))

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

(defn- confirm-options!
  [pid puri]
  (print (format "Do you want to initialize dtlv-surge in current folder with initial profile '%s' for database '%s'? (y/n) [y]: " pid puri))
  (loop [in (read-line)]  
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur))))

(defn- ask-options
  []
  (loop []
    (let [pid  (ask-profile-id!)
          puri (ask-profile-uri!)]
      (if (true? confirm-options!)
        [pid puri]
        (recur)))))

(defn- initialize
  []
  (let [[pid puri] (ask-options)]
    ))

(defn main
  []
  (if (check-initialization)
    (println "Looks like dtlv-surge is already initialized in this folder. Use 'dtlv-surge check' to check consistency or 'dtlv-surge clear' to clear all dtlv-surge data.")
    (initialize)))
