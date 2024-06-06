(ns datalevin-surge.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [leiningen.surge :refer [surge]]
            [datalevin-surge.vars :refer [*project*]]
            [datalevin-surge.test-helpers :refer [create-data-query with-test-case]]))

(with-test-case :case-1
  (surge *project* :test "check"))
