(defproject literate-clojure :lein-v
  :description "a literate programming tool to write clojure in org mode"
  :url "http://github.com/jingtaozf/literate-clojure"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[com.roomkey/lein-v "7.0.0"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]]
  :repositories [["snapshots" {:url "https://repo.clojars.org" :creds :gpg}]
                 ["releases" {:url "https://repo.clojars.org" :creds :gpg}]
                 ["alternate" {:url "https://repo.clojars.org" :creds :gpg}]]
  ;; :repl-options {:init-ns literate-clojure.core}
  )
