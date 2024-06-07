(ns datalevin-surge.test-helpers
  (:require [clojure.java.io :as io]
            [clojure.test :refer [is deftest]]
            [datalevin.core :as d]
            [datalevin-surge.vars :refer [*project*]]))

(def ^:private opts {:validate-data? true :closed-schema? true})

(defn create-data-query
  [schema]
  `[:find [(~'pull ~'?e [~'*]) ...]
    :where
    (~'or ~@(map (fn [a] `[~'?e ~a]) (keys schema)))])

(defmacro with-test-case
  [test-case & body]
  (let [{:keys [init-schema init-data exp-schema exp-data]}
        (->> (name test-case)
             (format "%s/data.edn")
             io/resource slurp read-string)

        dir (format "test/resources/%s/migrations/" (name test-case))
        uri (str "/tmp/datalevin-surge-test-" (java.util.UUID/randomUUID))]

    `(deftest ~(symbol (str "test-case-" (name test-case)))
       (try (d/with-conn [~'conn ~uri ~init-schema ~opts]
              (d/transact! ~'conn ~init-data))
            (binding [datalevin-surge.vars/*project* {:datalevin-surge
                                                      {:migrations-dir ~dir
                                                       :profiles
                                                       {:test ~uri}}}]
              ~@body)
            (d/with-conn [~'conn ~uri nil ~opts]
              (let [~'schema (d/schema ~'conn)
                    ~'query  (create-data-query ~'schema)]
                (when (some? ~exp-schema)
                  (is (= ~exp-schema ~'schema)))
                (when (some? ~exp-data)
                  (is (= ~exp-data (d/q ~'query (d/db ~'conn)))))))
            (finally (#'datalevin-surge.clear/del-dir-rec (io/file ~uri)))))))

(defn test-uri
  []
  (-> *project* :datalevin-surge :profiles :test))
