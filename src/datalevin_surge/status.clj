(ns datalevin-surge.status
  (:require [datalevin-surge.migration :as migr]
            [datalevin-surge.database :as db]
            [datalevin-surge.config :as conf]
            [datalevin-surge.profile :as prof]))

(defn process
  [pid]
  (let [local  (migr/sorted-local-migrations)
        remote (->> (db/read-kv pid)
                    (into [])
                    (map first)
                    set)]
    (printf "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n"
            (conf/migrations-dir)
            (prof/profile-uri pid))
    (doseq [mig local]
      (printf "[%s] %s\n"
              (if (some remote [(:uuid mig)]) "X" " ")
              (:filename mig)))))
