(ns datalevin-surge.check
  (:require [datalevin-surge.migration :as mgr]
            [datalevin.core :as d]
            [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn remote-init?
  [pid]
  (->> #{(db/raw-remote-schema pid) (db/internal-schema)}
       (map #(-> % keys set))
       (apply set/intersection)
       (= 2)))

(defn local-init?
  []
  (let [fs (io/file (conf/migrations-dir))]
    (and (.exists fs)
         (.isDirectory fs))))

(defn local-consistent?
  []
  (= (count (mgr/raw-local-migrations)) (count (mgr/sorted-local-migrations))))

(defn local-remote-consistent?
  [pid]
  (let [local  (map :uuid (mgr/sorted-local-migrations))
        remote (->> pid db/remote-connection d/db
                    (d/q '[:find (pull ?m [*])
                           :where
                           [?m :datalevin-surge-migration/uuid]])
                    (sort-by :timestamp)
                    (map :uuid))
        lcnt (count local)
        rcnt (count remote)]
    (cond
      (= local remote) {:ok true :message "Database is fully consistent to migrations"}
      (and (< lcnt rcnt) (= local (take lcnt remote))) {:ok false :message (format "Database is %d migrations ahead. Looks like you have to update your codebase." (- rcnt lcnt))}
      (and (> lcnt rcnt) (= (take rcnt local) remote)) {:ok true :message (format "Database is %d migrations below. You can use 'surge %s up' command to apply rest migrations." (- lcnt rcnt) pid)}
      :else {:ok false :message "Database and migrations are totally inconsistent! Better Error messsage will be implemented in further versions of plugin..."}))) ; TODO: Better error message

(defn main
  [pid]
  (println "HEY!!!!!!")
  (println (remote-init? pid))
  (println (local-init?))
  (println (local-consistent?))
  (println (local-remote-consistent? pid)))
