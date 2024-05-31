(ns datalevin-surge.core
  (:require [datalevin-surge.init :as init]))

(defn dispatch-category
  [args]
  (let [category  (first args)
        command   (second args)
        arguments (drop 2 args)]
    (cond
      (= "init" category) (init/main)

      (nil? category) (println "Empty category. See 'surge help'.")
      
      :else (println (format "'%s' is not a category. See 'surge help'." category)))))
