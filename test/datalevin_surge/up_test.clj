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
      (is (= "All up migrations are applied!\n"
             (with-out-str
               (migr/up *pid* :all))))))

  (with-test-case :150-up-1-2
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to apply 1 migration(s)? (y/n) \n[OK]\tMigration 0002-Create-accounts.edn successfully applied\n"
             (with-out-str
               (migr/up *pid* 1))))))

  (with-test-case :160-up-1-3-1
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to apply 1 migration(s)? (y/n) \n[OK]\tMigration 0002-Create-accounts.edn successfully applied\n"
             (with-out-str
               (migr/up *pid* 1))))))

  (with-test-case :170-up-1-3-all
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 3)
                  t/now (create-time 1)]
      (is (= "Do you want to apply 2 migration(s)? (y/n) \n[OK]\tMigration 0002-Create-accounts.edn successfully applied\n[OK]\tMigration 0003-Many-accounts.edn successfully applied\n"
             (with-out-str
               (migr/up *pid* :all))))))

  (with-test-case :180-up-1-3-error
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to apply 2 migration(s)? (y/n) \n[OK]\tMigration 0002-Create-accounts.edn successfully applied\n[ERROR]\tBad Migration 0003-Many-accounts.edn!\n\nclass java.lang.String cannot be cast to class java.math.BigDecimal (java.lang.String and java.math.BigDecimal are in module java.base of loader 'bootstrap')\n"
             (with-out-str
               (migr/up *pid* :all)))))))
