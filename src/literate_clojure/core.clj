(ns literate-clojure.core
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]]
    [clojure.pprint :refer [cl-format]]
    [clojure.string :refer [starts-with? lower-case trim]]
    )
  (:import (clojure.lang LispReader
                         LispReader$WrappingReader)))

(defn- line-terminator? [c]
  (or (= c (int \return)) (= c (int \newline))))
(defn- literate-read-line [reader]
  (let [c (.read reader)]
    (cond (= c -1) nil
          (line-terminator? c) ""
          :else (with-out-str
                  (do (cl-format *out* "~c" (char c))
                      (loop [c (.read reader)]
                        (when (and (not (= c -1))
                                   (not (line-terminator? c)))
                          (cl-format *out* "~c" (char c))
                          (recur (.read reader)))))))))

(defn- dispatch-reader-macro [ch fun]
  (let [dm (.get (doto (.getDeclaredField clojure.lang.LispReader "dispatchMacros")
                   (.setAccessible true))
                 nil)]
    (when (nil? (aget dm (int ch)))
      (debug (cl-format nil "install dispatch reader macro for character '~a'" ch))
      (aset dm (int ch) fun))))

(defn- dispatch-sharp-space [reader quote opts pending-forms]
  (debug "enter into org syntax.")
  (loop [line (literate-read-line reader)]
    (cond (nil? line) (debug "reach end of stream in org syntax.")
          (starts-with? (lower-case (trim line)) "#+begin_src clojure") (debug "reach begin of code block.")
          :else (do
                  (debug (cl-format nil "ignore line: ~a" line))
                  (recur (literate-read-line reader)))))

  (debug (cl-format nil "current line no:~s, column no:~s" (.getLineNumber reader) (.getColumnNumber reader)))
  (debug "enter into clojure syntax.")
  reader)


(defn- dispatch-sharp-plus [reader quote opts pending-forms]
  (let [line (literate-read-line reader)]
    (cond (nil? line) (debug "reach end of stream in org syntax.")
          (starts-with? (lower-case (trim line)) "end_src")
          (do (debug "reach begin of code block.")
              (debug "switch back from clojure syntax to org syntax.")
              (dispatch-sharp-space reader quote opts pending-forms))
          :else (throw (Exception. (cl-format nil "invalid syntax in line :~a" line))))))

(defn install-org-dispatcher []
  (dispatch-reader-macro \+ dispatch-sharp-plus)
  (dispatch-reader-macro \space dispatch-sharp-space))
