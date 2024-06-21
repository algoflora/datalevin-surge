(ns datalevin-surge.init-test
  (:require [clojure.test :refer [is testing]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [tick.core :as t]
            [datalevin-surge.misc :refer [ask-approve!]]
            [datalevin-surge.init :as init]
            [datalevin-surge.profile :as prof]
            [datalevin-surge.database :as db]
            [datalevin-surge.test-helpers :refer [with-test-case
                                                  *pid*
                                                  *dir*
                                                  approve!
                                                  create-uuid
                                                  create-time]]))

(testing "INIT command"
    (with-test-case :100-init-already-inited
      (is (str/starts-with?
           (with-out-str (init/process *pid*))
           "Looks like Datalevin Surge is already sucessfully initialized")))

    (with-test-case :110-init-error
      (is (= "[ERROR] Something wrong! Use 'surge check' to check!\n"
             (with-out-str (init/process *pid*)))))

    (with-test-case :120-init-no-remote
      (with-redefs [ask-approve! approve!]
        (is (false? (db/dbi-open? *pid*)))
        (doseq [m (mapv #(array-map :act %1 :exp %2)
                               (str/split-lines
                                (with-out-str
                                  (init/process *pid*)))
                               ["Do you want to initialize Datalevin Surge for database"])]
          (is (str/starts-with? (:act m) (:exp m))))
        (is (true? (db/dbi-open? *pid*)))))

    (with-test-case :130-init-virgin
      (with-redefs [ask-approve! approve!
                    random-uuid (create-uuid)
                    t/now (create-time)]
        (is (false? (db/dbi-open? *pid*)))
        (is (empty? (filter #(not (.isDirectory %)) (file-seq *dir*))))
        (doseq [m (mapv #(array-map :act %1 :exp %2)
                        (str/split-lines
                         (with-out-str
                           (init/process *pid*)))
                        [(format "Do you want to initialise Datalevin Surge migration tool in current folder and database '%s'? (y/n): " (prof/profile-uri *pid*))
                         (format "[OK]\t\"Initial migration\" migration created in file %s/0000-initial-migration.edn" *dir*)])]
          (is (= (:exp m) (:act m))))
        (is (true? (db/dbi-open? *pid*)))
        (let [mgs  (filter #(not (.isDirectory %)) (file-seq *dir*))
              cnt  (count mgs)
              mg   (first mgs)
              str  (slurp mg)
              mig  (-> mg io/reader java.io.PushbackReader. edn/read)
              header (str/join "\n" [";;; Datalevin Surge migration"
                                     ";;;"
                                     ";;;   Initial migration"
                                     ";;;"
                                     ";;;   Project: io.github.algoflora/datalevin-surge"
                                     ";;;"
                                     ";;;   Automatically created initial schema migration"
                                     ";;;"
                                     ";;;   Initialy created: 1970-01-01 00:00:01"])]
          (is (= 1 cnt))
          (is (str/starts-with? str header))
          (is (= #uuid "00000000-0000-0000-0000-000000000001" (:uuid mig)))
          (is (and (contains? mig :parent) (-> mig :parent nil?)))
          (is (= {:schema-update {:person/id {:db/valueType :db.type/long
                                              :db/cardinality :db.cardinality/one
                                              :db/unique :db.unique/identity}
                                  
                                  :person/name {:db/valueType :db.type/string
                                                :db/cardinality :db.cardinality/one}
                                  
                                  :person/amount {:db/valueType :db.type/bigdec
                                                  :db/cardinality :db.cardinality/one}}} (:up mig)))
          (is (= {:schema-remove #{:person/id :person/name :person/amount}} (:down mig)))))))
