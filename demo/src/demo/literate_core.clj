# -*- encoding:utf-8; Mode: ORG;  -*- ---
#+OPTIONS: toc:2 \n:nil @:t ::t |:t ^:nil -:t f:t *:t <:t
#+Startup: noindent
#+PROPERTY: literate-lang clojure
#+PROPERTY: literate-load yes
* Table of Contents                                            :TOC:noexport:
- [[#this-is-an-example][this is an example]]
- [[#list-all-possible-names][List all possible names.]]
- [[#this-is-a-test-code][this is a test code]]

* this is an example
#+BEGIN_SRC clojure
(ns demo.literate-core
  (:require [clojure.string :as str]))
#+END_SRC
define a function
#+BEGIN_SRC clojure
(defn main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
#+END_SRC
* List all possible names.
One person's name may be very long but in many connections one can use only parts of it.
For example a person named "A B C D E F" should produce this list of possible names:
#+begin_example
["A", "B", "C", "D", "E", "F"]
["A", "B", "C", "D", "F"]
["A", "B", "C", "E", "F"]
["A", "B", "C", "F"]
["A", "B", "D", "E", "F"]
["A", "B", "D", "F"]
["A", "B", "E", "F"]
["A", "B", "F"]
["A", "C", "D", "E", "F"]
["A", "C", "D", "F"]
["A", "C", "E", "F"]
["A", "C", "F"]
["A", "D", "E", "F"]
["A", "D", "F"]
["A", "E", "F"]
["A", "F"]
#+end_example
Can you find all possible combinations where my a middlename is mentioned or not.
First name and last name (in this case =A= and =F=) should always be there.
#+BEGIN_SRC clojure
(defn permutations [parts]
  (if (<= (count parts) 1)
    [() parts]
    (let [first-part (first parts)
          possible-parts-for-rest (permutations (rest parts))]
      (concat
       (map (fn[parts] (cons first-part parts)) possible-parts-for-rest)
       possible-parts-for-rest))))

(defn possible-names [name]
  (let [parts (str/split name #" ")
        first-part (first parts)
        last-part (last parts)]
    (map (fn[possible-parts] (cons first-part (concat possible-parts [last-part])))
         (permutations (butlast (rest parts))))))
#+END_SRC

* this is a test code

It will only be loaded when you set tag in a system environment variable ~LITERATE_LOAD_TAGS~ or a system property ~literate-clojure.load.tags~
#+BEGIN_SRC clojure :load dev
(println "I will only print to console in dev mode")
#+END_SRC
