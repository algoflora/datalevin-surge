(ns datalevin-surge.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [leiningen.surge :refer [surge]]
            [datalevin-surge.check :as check]
            [datalevin-surge.vars :refer [*project*]]
            [datalevin-surge.test-helpers :refer [with-test-case]]))

#_(deftest core-test
  (is (true? true)))
