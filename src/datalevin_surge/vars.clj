(ns datalevin-surge.vars)

(defmacro project-data
  []
  (let [proj (some-> "project.clj" slurp read-string)
        [_ ga] proj
        proj-map (->> proj (drop 3) (partition 2) vec (map vec) (into {}))]
    (assoc (:datalevin-surge proj-map) :name (name ga))))

(def ^:dynamic *project* (project-data))
