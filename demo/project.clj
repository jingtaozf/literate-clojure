(defproject demo :lein-v
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[com.roomkey/lein-v "7.0.0"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [literate-clojure "0.0.0-4-0x30b5-DIRTY"]]
  :main ^:skip-aot demo.core
  :target-path "target/%s"
  :repl-options {:init-ns demo.core}
  :profiles {:uberjar {:aot :all}})
