(defproject demo "0.0.1"
  :description "a demo project for library literate-clojure"
  :url "https://github.com/jingtaozf/literate-clojure"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [literate-clojure "0.1.1"]]
  :main ^:skip-aot demo.core
  :target-path "target/%s"
  :repl-options {:init-ns demo.core}
  :profiles {:uberjar {:aot :all}})
