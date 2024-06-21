(ns datalevin-surge.core
  (:require [datalevin-surge.init :as init]
            [datalevin-surge.new :as new]
            [datalevin-surge.check :as check :refer [with-check]]
            [datalevin-surge.migration :as migr]
            [datalevin-surge.status :as status]
            [datalevin-surge.vars :refer [*project*]]))

(defmulti ^:private process (fn [& args] (-> args first keyword)))

(defmethod process :check
  [_ pid args]
  (if (empty? args)
    (check/process pid true)
    (println "[ERROR]\t'check' command does not accept arguments!")))

(defmethod process :init
  [_ pid args]
  (if (empty? args)
    (with-check pid 
      #(init/process pid))
    (println "[ERROR]\t'init' command does not accept arguments!")))

(defn- migrate
  [pid side args]
  (cond
    (not= 1 (count args)) (println "[ERROR]\t'up' and 'down' commands awaiting exactly one argument!")
    
    (and (not= :all (first args))
         (pos-int? (first args)))
    (println "[ERROR]\t'up' and 'down' commands argument must be positive number or :all keyword!")

    :else (with-check pid
            #(case side
               :up   (migr/up pid (first args))
               :down (migr/down pid (first args))))))

(defmethod process :up
  [_ pid args]
  ()
  (migrate pid :up args))

(defmethod process :down
  [_ pid args]
  (migrate pid :down args))

(defmethod process :status
  [_ pid args]
  (if (empty? args)
    (with-check pid
      #(status/process pid))
    (println "[ERROR]\t'status' command does not accept arguments!")))

(defmethod process :new
  [_ pid args]
  (cond
    (empty? args) (with-check pid
                    #(new/process))

    (= 1 (count args)) (with-check pid
                         #(new/process (first args)))

    :else (println "[ERROR]\t'new' command accepts only one optional migration name argument!")))

(defn -main
  [args]
  (let [profile   (first args)
        command   (second args)
        arguments (drop 2 args)]
    (if (some (-> *project* :datalevin-surge :profiles keys set) [profile])
      (process command profile arguments)
      (println (format "[ERROR]\tWrong profile '%d'!" profile)))))
