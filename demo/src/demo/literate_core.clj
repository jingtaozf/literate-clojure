# -*- encoding:utf-8; Mode: ORG;  -*- --- 
#+OPTIONS: toc:2 \n:nil @:t ::t |:t ^:nil -:t f:t *:t <:t
#+Startup: noindent
#+PROPERTY: literate-lang clojure
#+PROPERTY: literate-load yes
* this is an example
#+BEGIN_SRC clojure
(ns demo.literate-core)
#+END_SRC
define a function
#+BEGIN_SRC clojure
(defn main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
#+END_SRC
* this is a test code

It will only be loaded when you set tag in a system environment variable ~LITERATE_LOAD_TAGS~ or a system property ~literate-clojure.load.tags~
#+BEGIN_SRC clojure :load dev
(println "I will only print to console in dev mode")
#+END_SRC
