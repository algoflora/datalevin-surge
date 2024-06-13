(ns datalevin-surge.init-test
  (:require [clojure.test :refer [is testing]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [datalevin-surge.init :as init]
            [datalevin-surge.database :as db]
            [datalevin-surge.test-helpers :refer [with-test-case *pid* *dir*]]))

(testing "Init command"
    (with-test-case :100-init-already-inited
      (is (str/starts-with?
           (with-out-str (init/process *pid*))
           "Looks like Datalevin Surge is already sucessfully initialized")))

    (with-test-case :110-init-error
      (is (= "[ERROR] Something wrong! Use 'surge check' to check!\n"
             (with-out-str (init/process *pid*)))))

    (with-test-case :120-init-no-remote
      (with-redefs [datalevin-surge.init/ask-approve! (fn [s] (println s) true)]
        (is (false? (db/dbi-open? *pid*)))
        (doseq [m (mapv #(array-map :act %1 :exp %2)
                               (str/split-lines
                                (with-out-str
                                  (init/process *pid*)))
                               ["Do you want to initialize Datalevin Surge for database"])]
          (is (str/starts-with? (:act m) (:exp m))))
        (is (true? (db/dbi-open? *pid*)))))

    (with-test-case :130-init-virgin
      (with-redefs [datalevin-surge.init/ask-approve! (fn [s] (println s true))]
        (is (false? (db/dbi-open? *pid*)))
        (is (empty? (filter #(not (.isDirectory %)) (file-seq *dir*))))
        (doseq [m (mapv #(array-map :act %1 :exp %2)
                               (str/split-lines
                                (with-out-str
                                  (init/process *pid*)))
                               ["Do you want to initialise Datalevin Surge migration tool in current folder and database"])]
          (is (str/starts-with? (:act m) (:exp m))))
        (is (true? (db/dbi-open? *pid*)))
        (is (= 1 (count (filter #(not (.isDirectory %)) (file-seq *dir*))))))))
