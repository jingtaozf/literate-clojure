# -*- encoding:utf-8 Mode: POLY-ORG;  -*- --- Enter into org syntax
#+Startup: noindent
* This is a test head line
** define a new namespace
#+BEGIN_SRC clojure
(ns literate-clojure.core-test
  (:require [clojure.test :refer :all]
            [literate-clojure.core :refer :all]))
#+END_SRC
** define a new test
#+BEGIN_SRC clojure
(deftest test1
  (testing "test1"
    (is (= 1 1))))
#+END_SRC
# An org line to ignore.

# #+BEGIN_SRC clojure :tangle no 
# (clojure.pprint.cl-format true "A test message from test.org~%")
# #+END_SRC
# An org line to ignore.
