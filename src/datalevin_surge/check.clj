(ns datalevin-surge.check
  (:require [datalevin-surge.migration :as mgr]
            [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [clojure.java.io :as io]))

(defn remote-init?
  [pid]
  (if (db/dbi-open? pid)
    {:ok true  :message "Remote is initialised"}
    {:ok false :message "Remote is not initialised!"}))

(defn local-init?
  []
  (let [fs      (io/file (conf/migrations-dir))
        is-dir? (and (.exists fs)
                     (.isDirectory fs))]
    (if is-dir?
      (let [n (->> (mgr/raw-local-migrations)
                   (filter #(and (contains? % :parent) (nil? (:parent %))))
                   count)]
        (case n
          1 {:ok true  :message "Migration files are initialised with initial migration"}
          0 {:ok false :message "Migration files are not initialised! Use 'surge <:profile> init' command."}
          {:ok false :message (format "Migration files are initialised wrong! %d initial migration files." n)}))
      {:ok false :message (format "Folder %s does not exist!" (conf/migrations-dir))})))

(defn local-consistent?
  []
  (try
    (if (= (count (mgr/raw-local-migrations)) (count (mgr/sorted-local-migrations)))
      {:ok true :message "Local migration files are consistent"}
      {:ok false :message "Extra EDN-files in migrations folder!"})
    (catch Exception ex
      {:ok false :message (str (.getMessage ex) "\t" (ex-data ex))})))

(defn local-remote-consistent?
  [pid]
  (let [local  (map :uuid (mgr/sorted-local-migrations))
        remote (->> (db/read-kv pid)
                    (into [])
                    (sort-by second)
                    (map first))
        lcnt (count local)
        rcnt (count remote)]
    (cond
      (= local remote) {:ok true :message "Migrations and database are consistent"}
      (and (< lcnt rcnt) (= local (take lcnt remote))) {:ok false :message (format "Database is %d migrations ahead. Looks like you have to update your codebase." (- rcnt lcnt))}
      (and (> lcnt rcnt) (= (take rcnt local) remote)) {:ok true :message (format "Database is %d migrations behind. You can use 'surge %s up' command to apply rest migrations." (- lcnt rcnt) pid)}
      :else {:ok false :message "Database and migrations are inconsistent! Better Error messsage will be implemented in further versions of plugin..."}))) ; TODO: Better error message

(defn- check-flow
  [print? & funcs]
  (loop [a {:ok true :messages []}
         f funcs]
    (cond
      (nil? (first f)) a
      (not (:ok a))    a
      :else            (let [result ((first f))]
                         (when print?
                           (print (if (:ok result) "[OK]\t" "[ERROR]\t"))
                           (println (:message result)))
                         (recur {:ok (and (:ok a) (:ok result))
                                 :messages (conj (:messages a) (:message result))} (rest f))))))


(defn process
  [pid print?]
  (check-flow print?
              #(remote-init? pid)
              #(local-init?)
              #(local-consistent?)
              #(local-remote-consistent? pid)))

(defn main
  [pid]
  (process pid true))
