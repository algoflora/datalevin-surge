(ns datalevin-surge.databases
  (:require [datalevin.core :as d]
            [datalevin-surge.config :as conf]))

(defn migrations-conn (d/get-conn conf/database-dir conf/database-schema {:validate-data? true
                                                                          :closed-schema? true}))
