(ns demo.core
  (:require [literate-clojure.core]
            [demo.literate-core :refer [main]])
  (:gen-class))
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (main))
