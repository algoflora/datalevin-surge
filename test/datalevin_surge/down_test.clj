(ns datalevin-surge.down-test
  (:require [clojure.test :refer [is testing]]
            [tick.core :as t]
            [datalevin-surge.misc :refer [ask-approve!]]
            [datalevin-surge.migration :as migr]
            [datalevin-surge.test-helpers :refer [with-test-case
                                                  *pid*
                                                  approve!
                                                  create-uuid
                                                  create-time]]))

(testing "UP command"
  (with-test-case :190-down-no-need
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid)
                  t/now (create-time)]
      (is (= "No migrations to revert!\n"
             (with-out-str
               (migr/down *pid* :all))))))

  (with-test-case :200-down-2-1
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 1 migration(s)? (y/n) \n[OK]\tMigration 0002-Create-accounts.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* 1))))))

  (with-test-case :210-down-3-1-1
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 1 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* 1))))))

  (with-test-case :220-down-3-1-all
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 3)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 3 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n[OK]\tMigration 0002-Create-accounts.edn successfully reverted\n[OK]\tMigration 0001-Initial.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* :all))))))

  (with-test-case :221-down-3-1-3
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 3)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 3 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n[OK]\tMigration 0002-Create-accounts.edn successfully reverted\n[OK]\tMigration 0001-Initial.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* 3))))))

  (with-test-case :222-down-3-1-10
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 3)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 3 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n[OK]\tMigration 0002-Create-accounts.edn successfully reverted\n[OK]\tMigration 0001-Initial.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* 10))))))

  (with-test-case :230-down-3-1-2
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 3)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 2 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n[OK]\tMigration 0002-Create-accounts.edn successfully reverted\n"
             (with-out-str
               (migr/down *pid* 2))))))

  (with-test-case :240-down-3-1-error
    (with-redefs [ask-approve! approve!
                  random-uuid (create-uuid 2)
                  t/now (create-time 1)]
      (is (= "Do you want to revert 3 migration(s)? (y/n) \n[OK]\tMigration 0003-Many-accounts.edn successfully reverted\n[ERROR]\tBad Migration 0002-Create-accounts.edn!\n\nclass java.lang.String cannot be cast to class java.math.BigDecimal (java.lang.String and java.math.BigDecimal are in module java.base of loader 'bootstrap')\n"
             (with-out-str
               (migr/down *pid* :all)))))))
