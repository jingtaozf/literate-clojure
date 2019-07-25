(defproject literate-clojure "0.2.1"
  :description "a literate programming tool to write clojure in org mode"
  :url "http://github.com/jingtaozf/literate-clojure"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :repositories [["snapshots" {:url "https://repo.clojars.org" :creds :gpg}]
                 ["releases" {:url "https://repo.clojars.org" :creds :gpg}]
                 ["alternate" {:url "https://repo.clojars.org" :creds :gpg}]]
  :profiles {:uberjar {:omit-source true :aot :all}}
  :repl-options {:init-ns literate-clojure.core}
  )
