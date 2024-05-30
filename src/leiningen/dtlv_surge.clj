(ns leiningen.dtlv-surge
  (:require [leiningen.dtlv-surge.init :as init]))

(defn- dispatch-category
  [args]
  (let [category  (first args)
        command   (second args)
        arguments (drop 2 args)]
    (cond
      (= "init" category) (init/main)

      (nil? category) (println "Empty category. See 'dtlv-surge help'.")
      
      :else (println (format "'%s' is not a category. See 'dtlv-surge help'." category)))))

(defn dtlv-surge
  
  "I don't do a lot."

  [project & args]
  (println "Hi!"))
