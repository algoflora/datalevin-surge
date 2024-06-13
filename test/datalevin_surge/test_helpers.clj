(ns datalevin-surge.test-helpers
  (:require [clojure.java.io :as io]
            [clojure.test :refer [is deftest]]
            [datalevin.core :as d]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
            [clojure.string :as s]))

(def ^:private opts {:validate-data? true :closed-schema? true})

(defonce ^:dynamic *pid* nil)
(defonce ^:dynamic *dir* nil)

(defn- copy-file [src dest]
  (io/copy (io/file src) (io/file dest)))

(defn- copy-dir-recursively
  ([src-dir dest-dir] (copy-dir-recursively src-dir dest-dir ""))
  ([src-dir dest-dir ext]
   (let [src (io/file src-dir)
         dest (io/file dest-dir)]
     (when-not (.exists dest)
       (.mkdirs dest))
     (doseq [file (.listFiles src)]
       (let [dest-file (io/file dest (.getName file))]
         (if (.isDirectory file)
           (copy-dir-recursively file dest-file ext)
           (when (clojure.string/ends-with? file ext)
             (copy-file file dest-file))))))))

(defn- create-data-query
  [schema]
  `[:find [(~'pull ~'?e [~'*]) ...]
    :where
    (~'or ~@(map (fn [a] `[~'?e ~a]) (keys schema)))])

(defmacro with-test-case
  [test-case & body]
  (require '[datalevin-surge.clear])
  (let [{:keys [init-kv init-schema init-data exp-schema exp-data]}
        (->> (name test-case)
             (format "%s/data.edn")
             io/resource slurp read-string)

        src  (format "test/resources/%s/migrations/" (name test-case))
        uuid (java.util.UUID/randomUUID)
        dir  (str "/tmp/datalevin-surge-test-" (name test-case) "-" uuid "-dir")
        uri  (str "/tmp/datalevin-surge-test-" (name test-case) "-" uuid "-uri")]

    `(deftest ~(symbol (str "test-case-" (name test-case)))
       (try
         (#'datalevin-surge.test-helpers/copy-dir-recursively ~src ~dir ".edn")
         (when (some? ~init-kv)
           (let [~'kv (d/open-kv ~uri)]
             (d/open-dbi ~'kv conf/dbi-name)
             (d/transact-kv
              ~'kv
              (mapv #(vec (concat [:put conf/dbi-name] %)) ~init-kv))
             (d/close-kv ~'kv)))
         (d/with-conn [~'conn ~uri ~init-schema ~opts]
           (d/transact! ~'conn ~init-data))
         (binding [datalevin-surge.vars/*project* {:datalevin-surge
                                                   {:migrations-dir ~dir
                                                    :profiles
                                                    {~test-case ~uri}}}
                   *pid* ~test-case
                   *dir* (clojure.java.io/file ~dir)]
           ~@body)
         (d/with-conn [~'conn ~uri nil ~opts]
           (let [~'schema (d/schema ~'conn)
                 ~'query  (#'datalevin-surge.test-helpers/create-data-query ~'schema)]
             (when (some? ~exp-schema)
               (is (= ~exp-schema ~'schema)))
             (when (some? ~exp-data)
               (is (= ~exp-data (d/q ~'query (d/db ~'conn)))))))
         (finally (#'datalevin-surge.clear/del-dir-rec (io/file ~uri))
                  (#'datalevin-surge.clear/del-dir-rec (io/file ~dir)))))))

(defn test-uri
  []
  (-> *project* :datalevin-surge :profiles :test))
