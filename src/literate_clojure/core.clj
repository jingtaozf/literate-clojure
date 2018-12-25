;; (load-file "src/literate_clojure/core.org")
(ns literate-clojure.core
  (:use    [clojure.string]
           [clojure.pprint])
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]])
  (:import (clojure.lang LispReader
                         LispReader$WrappingReader)))

(defn literate-read-line [reader]
  (let [c (.read reader)]
    (cond (= c -1) nil
          (= c (int \newline)) ""
          :else (with-out-str
                  (do (cl-format *out* "~c" (char c))
                      (loop [c (.read reader)]
                        (cond (= c -1) nil
                              (= c (int \newline)) nil
                              :else (do (cl-format *out* "~c" (char c))
                                        (recur (.read reader))))))))))

(defn dispatch-reader-macro [ch fun]
  (let [dm (.get (doto (.getDeclaredField clojure.lang.LispReader "dispatchMacros")
                   (.setAccessible true))
                 nil)]
    (debug (cl-format nil "set dispatch reader macro to character '~a'" ch))
    (aset dm (int ch) fun)))

(defn dispatch-sharp-space [reader quote opts pending-forms]
  (debug "enter into org syntax.")
  (loop [line (literate-read-line reader)
         trimmed-line (trim line)]
    (when (and line (not (starts-with? (lower-case trimmed-line) "#+begin_src clojure")))
      (debug (cl-format nil "ignore line: ~a" line))
      (recur (literate-read-line reader) (trim line))))

  (debug "enter into clojure syntax.")
  nil)
(dispatch-reader-macro \  dispatch-sharp-space)

(defn dispatch-sharp-plus [reader quote opts pending-forms]
  (let [line (literate-read-line reader)]
    (debug (cl-format nil "ignore line:~a" line))
    (debug "switch back from clojure syntax to org syntax."))
  (dispatch-sharp-space reader quote opts pending-forms))
(dispatch-reader-macro \+ dispatch-sharp-plus)

(load-file "src/literate_clojure/example.clj")
