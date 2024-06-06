(ns datalevin-surge.core
  (:require [datalevin-surge.init :as init]
            [datalevin-surge.check :as check]))

(defn dispatch-category
  [args]
  (let [profile   (first args)
        category  (second args)
        command   (get args 2)
        arguments (drop 3 args)]
    (cond
      ;(= "init" category) (init/main)
      (= "check" category) (check/main profile)
      
      (nil? category) (println "Empty category. See 'surge help'.")
      
      :else (println (format "'%s' is not a category. See 'surge help'." category)))))
