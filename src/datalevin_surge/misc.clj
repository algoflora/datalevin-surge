(ns datalevin-surge.misc
  (:require [clojure.java.io :as io]))

(defn ask-approve!
  [prompt]
  (print prompt)
  (loop [in (read-line)]
    (cond (= "y" in) true
          (= "n" in) false
          :else (recur (read-line)))))

(defn del-dir-rec
  "Recursively delete a directory."
  [^java.io.File file]
  (when (.isDirectory file)
    (run! del-dir-rec (.listFiles file)))
  (when (.exists file)
    (io/delete-file file)))
