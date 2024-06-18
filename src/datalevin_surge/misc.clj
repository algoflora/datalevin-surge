(ns datalevin-surge.misc)

(defn ask-approve!
  [prompt]
  (print prompt)
  (loop [in (read-line)]
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur (read-line)))))
