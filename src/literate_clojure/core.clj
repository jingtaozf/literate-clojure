;;; This file is automatically generated from file `src/literate_clojure/core.org'.
;;; It is not designed to be readable by a human.
;;; It is generated to load by clojure directly without depending on `literate-clojure'.
;;; Please read file `src/literate_clojure/core.org' to find out the usage and implementation detail of this source file.

(ns literate-clojure.core
  (:require
    [taoensso.timbre :as timbre :refer [debug get-env]]
    [clojure.pprint :refer [cl-format]]
    [clojure.string :refer [starts-with? lower-case trim split]])
  (:import (clojure.lang LispReader LispReader$WrappingReader)))

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

(defn- load? [arguments]
  (debug (cl-format nil "header arguments is: ~s" arguments))
  (loop [left-arguments arguments]
    (cond (nil? left-arguments) true
          (= (first left-arguments) ":load")
          (case (second left-arguments)
            nil true
            "" true
            "yes" true
            "no" nil)
          :else (recur (next left-arguments)))))

(def id-of-begin-src "#+begin_src clojure")
(defn- read-org-code-block-header-arguments [line]
  (let [trimmed-line (trim line)]
    (split (lower-case (.substring trimmed-line (.length id-of-begin-src))) #"\s+")))

(defn- dispatch-sharp-space [reader quote opts pending-forms]
  (debug "enter into org syntax.")
  (loop [line (literate-read-line reader)]
    (cond (nil? line) (debug "reach end of stream in org syntax.")
          (starts-with? (lower-case (trim line)) id-of-begin-src)
          (do (debug "reach begin of code block.")
              (if (load? (read-org-code-block-header-arguments line))
                (do 
                  (debug (cl-format nil "current line no:~s, column no:~s" (.getLineNumber reader) (.getColumnNumber reader)))
                  (debug "enter into clojure syntax."))
                (recur (literate-read-line reader))))
          :else (do
                  (debug (cl-format nil "ignore line: ~a" line))
                  (recur (literate-read-line reader)))))
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

(def exception-id-of-end-of-stream "end-of-litereate-stream")
(defn tangle-file [org-file]
  (with-open [reader (clojure.lang.LineNumberingPushbackReader. (clojure.java.io/reader org-file))]
    (with-open [writer (clojure.java.io/writer (str (.substring org-file 0 (.lastIndexOf org-file "."))
                                                    ".clj"))]
      (.write writer (cl-format nil ";;; This file is automatically generated from file `~a'.
;;; It is not designed to be readable by a human.
;;; It is generated to load by clojure directly without depending on `literate-clojure'.
;;; Please read file `~a' to find out the usage and implementation detail of this source file.~%~%"
                                org-file org-file))
      (try
        (while true
          ;; ignore all lines of org syntax.
          (dispatch-sharp-space reader \space nil nil)
          ;; start to read clojure codes.
          (loop [line (literate-read-line reader)]
            (cond (nil? line) (do (debug "reach end of stream in org syntax.")
                                  (throw (Exception. exception-id-of-end-of-stream)))
                  (starts-with? (lower-case (trim line)) "#+end_src")
                  (debug "reach end of code block.")
                  :else (do
                          (debug (cl-format nil "tangle line: ~a" line))
                          (.write writer line)
                          (.write writer "\n")
                          (recur (literate-read-line reader)))))
          (.write writer "\n")
          (.flush writer))
        (catch Exception e
          (if (not (= exception-id-of-end-of-stream (.getMessage e)))
            ;; we don't know about this exception, throw it again.
            (throw e)))))))

