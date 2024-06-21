(ns datalevin-surge.new-test
  (:require [clojure.test :refer [is testing]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [tick.core :as t]
            [datalevin-surge.new :as new]
            [datalevin-surge.test-helpers :refer [with-test-case
                                                  *dir*
                                                  create-uuid
                                                  create-time]]))

(testing "NEW command"
  (with-test-case :300-new
    (with-redefs [random-uuid (create-uuid 1)
                  t/now (create-time)]
      (is (= (format "[OK]\t\"Migration to the brave new world\" migration created in file %s/0001-migration-to-the-brave-new-world.edn\n" *dir*)
             (with-out-str
               (new/process "Migration to the brave new world"))))
      (let [mgs  (filter #(not (.isDirectory %)) (file-seq *dir*))
            cnt  (count mgs)
            mg   (second (sort-by #(.getName %) mgs))
            str  (slurp mg)
            mig  (-> mg io/reader java.io.PushbackReader. edn/read)
            content (str/join "\n"
                              [";;; Datalevin Surge migration"
                               ";;;"
                               ";;;   Migration to the brave new world"
                               ";;;"
                               ";;;   Project: io.github.algoflora/datalevin-surge"
                               ";;;"
                               ";;;   I'am doing nothing FIXME"
                               ";;;"
                               ";;;   Initialy created: 1970-01-01 00:00:01"
                               ""
                               "{:uuid #uuid \"00000000-0000-0000-0000-000000000002\" ; DO NOT EDIT!"
                               ""
                               " :parent #uuid \"00000000-0000-0000-0000-000000000001\""
                               ""
                               " :up {:stage-fn nil"
                               ""
                               "      :schema-remove nil"
                               ""
                               "      :schema-insert nil"
                               ""
                               "      :unstage-fn nil}"
                               ""
                               " :down {:stage-fn nil"
                               ""
                               "        :schema-remove nil"
                               ""
                               "        :schema-insert nil"
                               ""
                               "        :unstage-fn nil}}\n"])]
        (is (= 2 cnt))
        (is (= "0001-migration-to-the-brave-new-world.edn" (.getName mg)))
        (is (= str content))
        (is (= #uuid "00000000-0000-0000-0000-000000000002" (:uuid mig)))
        (is (and (contains? mig :parent) (= #uuid "00000000-0000-0000-0000-000000000001" (:parent mig))))))))
