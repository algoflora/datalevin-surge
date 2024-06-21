(ns datalevin-surge.status-test
  (:require [clojure.test :refer [is testing]]
            [datalevin-surge.test-helpers :refer [with-test-case
                                                  *pid*
                                                  *dir*]]
            [datalevin-surge.profile :as prof]
            [datalevin-surge.status :as status]))

(testing "STATUS command"
  (with-test-case :250-status-0-3
    (is (= (format "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n[ ] 0001-Initial.edn\n[ ] 0002-Create-accounts.edn\n[ ] 0003-Many-accounts.edn\n" *dir* (prof/profile-uri *pid*))
           (with-out-str
             (status/process *pid*)))))

  (with-test-case :260-status-1-3
    (is (= (format "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n[X] 0001-Initial.edn\n[ ] 0002-Create-accounts.edn\n[ ] 0003-Many-accounts.edn\n" *dir* (prof/profile-uri *pid*))
           (with-out-str
             (status/process *pid*)))))

  (with-test-case :270-status-2-3
    (is (= (format "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n[X] 0001-Initial.edn\n[X] 0002-Create-accounts.edn\n[ ] 0003-Many-accounts.edn\n" *dir* (prof/profile-uri *pid*))
           (with-out-str
             (status/process *pid*)))))

  (with-test-case :280-status-3-3
    (is (= (format "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n[X] 0001-Initial.edn\n[X] 0002-Create-accounts.edn\n[X] 0003-Many-accounts.edn\n" *dir* (prof/profile-uri *pid*))
           (with-out-str
             (status/process *pid*)))))

  (with-test-case :290-status-1-2
    (is (= (format "Local:\t%s\nRemote:\t%s\n\nMigrations statuses:\n\n[X] 0001-Initial.edn\n[ ] 0002-Create-accounts.edn\n" *dir* (prof/profile-uri *pid*))
           (with-out-str
             (status/process *pid*))))))
