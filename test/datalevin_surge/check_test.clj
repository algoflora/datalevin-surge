(ns datalevin-surge.check-test
  (:require [clojure.test :refer [is testing]]
            [datalevin-surge.check :as check]
            [datalevin-surge.test-helpers :refer [with-test-case *pid*]]))

(testing "CHECK command"

  (with-test-case :000-check-remote-not-initialised
    (is (= {:ok false :messages ["Remote is not initialised!"]}
           (check/process *pid* false))))

  (with-test-case :010-check-local-no-folder
    (let [result (check/process *pid* false)]
      (is (false? (:ok result)))
      (is (= "Remote is initialised" (-> result :messages (get 0))))
      (is (some? (re-matches #"Folder .+ does not exist!" (-> result :messages (get 1)))))))

  (with-test-case :020-check-local-not-initialised
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are not initialised! Use 'surge <:profile> init' command."]}
           (check/process *pid* false))))

  (with-test-case :030-check-local-wrong-initialised
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are initialised wrong! 3 initial migration files."]}
           (check/process *pid* false))))

  (with-test-case :040-check-local-extra-files
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are initialised with initial migration"
                                 "Extra EDN-files in migrations folder!"]}
           (check/process *pid* false))))

  (with-test-case :050-check-local-same-parent
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are initialised with initial migration"
                                 "Several migration files with same :parent!\t{:count 2, :files (\"0002-Second-copy.edn\" \"0002-Second.edn\")}"]}
           (check/process *pid* false))))

  (with-test-case :060-check-remote-wrong-order
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are initialised with initial migration"
                                 "Local migration files are consistent"
                                 "Database and migrations are inconsistent! Better Error messsage will be implemented in further versions of plugin..."]}
           (check/process *pid* false))))

  (with-test-case :070-check-remote-ahead
    (is (= {:ok false :messages ["Remote is initialised"
                                 "Migration files are initialised with initial migration"
                                 "Local migration files are consistent"
                                 "Database is 1 migrations ahead. Looks like you have to update your codebase."]}
           (check/process *pid* false))))

  (with-test-case :080-check-remote-behind
    (is (= {:ok true :messages ["Remote is initialised"
                                "Migration files are initialised with initial migration"
                                "Local migration files are consistent"
                                "Database is 2 migrations behind. You can use 'surge :080-check-remote-behind up' command to apply rest migrations."]}
           (check/process *pid* false))))

  (with-test-case :090-check-all-consistent
    (is (= {:ok true :messages ["Remote is initialised"
                                "Migration files are initialised with initial migration"
                                "Local migration files are consistent"
                                "Migrations and database are consistent"]}
           (check/process *pid* false)))))
