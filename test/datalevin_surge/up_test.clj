(ns datalevin-surge.up-test
  (:require [clojure.test :refer [is testing]]
            [tick.core :as t]
            [datalevin-surge.misc :refer [ask-approve!]]
            [datalevin-surge.migration :as migr]
            [datalevin-surge.test-helpers :refer [with-test-case
                                                  *pid*
                                                  *dir*
                                                  approve!
                                                  create-uuid
                                                  create-time]]))

(testing "UP command"
  (with-test-case :140-up-no-need
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid)
                  t/now (create-time)]
      (is (= "All up migrations are completed!\n"
             (with-out-str
               (migr/process *pid* :up)))))))
