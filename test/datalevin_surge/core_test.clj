(ns datalevin-surge.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [leiningen.surge :refer [surge]]
            [datalevin-surge.check :as check]
            [datalevin-surge.vars :refer [*project*]]
            [datalevin-surge.test-helpers :refer [with-test-case]]))

(with-test-case :000-check-remote-not-initialised
  (is (= {:ok false :messages ["Remote is not initialised!"]}
         (check/process :test false)))
  (surge *project* :test "check"))

(with-test-case :010-check-local-no-folder
  (is (= {:ok false :messages ["Remote is initialised"
                               "Folder ./test/resources/010-check-local-no-folder/migrations/ does not exist!"]}
         (check/process :test false)))
  (surge *project* :test "check"))

(with-test-case :020-check-local-not-initialised
  (is (= {:ok false :messages ["Remote is initialised"
                               "Migration files are not initialised! Use 'surge <:profile> init' command."]}
         (check/process :test false)))
  (surge *project* :test "check"))

(with-test-case :030-check-local-wrong-initialised
  (is (= {:ok false :messages ["Remote is initialised"
                               "Migration files are initialised wrong! 3 initial migration files."]}
         (check/process :test false)))
  (surge *project* :test "check"))

