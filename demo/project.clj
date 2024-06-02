(defproject demo "0.0.1"
  :description "a demo project for library literate-clojure"
  :url "https://github.com/jingtaozf/literate-clojure"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [literate-clojure "0.3.1"]
                 [dorothy "0.0.6"]
                 [org.clojure/data.priority-map "1.0.0"]
                 ]
  :injections [(require 'literate-clojure.core)]
  :main ^:skip-aot demo.core
  :target-path "target/%s"
  :repl-options {:init-ns demo.core}
  :profiles {:uberjar {:omit-source true :aot :all}})
