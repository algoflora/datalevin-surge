(ns datalevin-surge.test-helpers
  (:require [clojure.java.io :as io]
            [clojure.test :refer [is deftest]]
            [datalevin.core :as d]
            [datalevin-surge.config :as conf]
            [datalevin-surge.vars :refer [*project*]]
            [tick.core :as t]))

(def ^:private opts {:validate-data? true :closed-schema? true})

(defonce ^:dynamic *pid* nil)
(defonce ^:dynamic *dir* nil)

(defn approve!
  [s]
  (println s)
  true)

(defn create-uuid
  ([] (create-uuid 0))
  ([n]
   (let [uuids-num (atom n)]
     (fn []
       (java.util.UUID/fromString (format "00000000-0000-0000-0000-%012d" (swap! uuids-num inc)))))))

(defn create-time
  ([] (create-time 0))
  ([n]
   (let [counter (atom n)]
     (fn []
       (t/>> (t/instant 0) (t/new-duration (swap! counter inc) :seconds))))))

(defn- copy-file [src dest]
  (io/copy (io/file src) (io/file dest)))

(defn- copy-dir-recursively
  ([src-dir dest-dir] (copy-dir-recursively src-dir dest-dir #".*"))
  ([src-dir dest-dir re]
   (let [src (io/file src-dir)
         dest (io/file dest-dir)]
     (when-not (.exists dest)
       (.mkdirs dest))
     (doseq [file (.listFiles src)]
       (let [dest-file (io/file dest (.getName file))]
         (if (.isDirectory file)
           (copy-dir-recursively file dest-file re)
           (when (re-matches re (.getName file))
             (copy-file file dest-file))))))))

(defn- create-data-query
  [schema]
  (when-not (empty? schema)
    `[:find [(~'pull ~'?e [~'*]) ...]
      :where
      (~'or ~@(map (fn [a] `[~'?e ~a]) (keys schema)))]))

(defmacro with-test-case
  [test-case & body]
  (require '[datalevin-surge.clear])
  (let [{:keys [init-kv init-schema init-data exp-kv exp-schema exp-data]}
        (->> (name test-case)
             (format "%s/data.edn")
             io/resource slurp read-string)

        src  (format "./test/resources/%s/migrations/" (name test-case))
        uuid (random-uuid)
        dir  (format "/tmp/datalevin-surge-test-%s-%s-dir" (name test-case) uuid)
        uri  (format "/tmp/datalevin-surge-test-%s-%s-uri" (name test-case) uuid)]

    `(deftest ~(symbol (str "test-case-" (name test-case)))
       (t/with-clock (-> (t/clock) (t/in "UTC"))
         (try
           (when (-> ~src io/file .isDirectory)
             (#'datalevin-surge.test-helpers/copy-dir-recursively ~src ~dir #"^[^\.].*\.edn$"))
           (when (some? ~init-kv)
             (let [~'kv (d/open-kv ~uri)]
               (d/open-dbi ~'kv conf/dbi-name)
               (d/transact-kv
                ~'kv
                (mapv #(vec (concat [:put conf/dbi-name] %)) ~init-kv))
               (d/close-kv ~'kv)))
           (d/with-conn [~'conn ~uri ~init-schema ~opts]
             (d/transact! ~'conn ~init-data))
           (binding [datalevin-surge.vars/*project* {:name "io.github.algoflora/datalevin-surge"
                                                     :datalevin-surge
                                                     {:migrations-dir ~dir
                                                      :profiles
                                                      {~test-case ~uri}}}
                     *pid* ~test-case
                     *dir* (clojure.java.io/file ~dir)]
             ~@body)
           (d/with-conn [~'conn ~uri nil ~opts]
             (let [~'schema (into {} (map (fn [[~'k ~'v]] [~'k (dissoc ~'v :db/aid)])) (dissoc (d/schema ~'conn) :db/created-at :db/ident :db/updated-at))
                   ~'query  (#'datalevin-surge.test-helpers/create-data-query ~'schema)]
               (when (some? ~exp-schema)
                 (is (= ~exp-schema ~'schema)))
               (when (some? ~exp-data)
                 (is (= (set ~exp-data) (if ~'query (set (d/q ~'query (d/db ~'conn))) #{})))))
             (when (some? ~exp-kv)
               (let [~'kv-conn (d/open-kv ~uri)]
                 (try
                   (d/open-dbi ~'kv-conn conf/dbi-name)
                   (is (= ~exp-kv (d/get-range ~'kv-conn conf/dbi-name [:all] :data :data)))
                   (finally (d/close-kv ~'kv-conn))))))
           (finally (#'datalevin-surge.clear/del-dir-rec (io/file ~uri))
                    (#'datalevin-surge.clear/del-dir-rec (io/file ~dir))))))))

(defn test-uri
  []
  (-> *project* :datalevin-surge :profiles :test))
