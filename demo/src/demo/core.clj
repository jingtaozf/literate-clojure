(ns demo.core
  (:require [literate-clojure.core :refer [install-org-dispatcher]]))
(install-org-dispatcher)
(load-file "src/demo/core.org")
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (main))
