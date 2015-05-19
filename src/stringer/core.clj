(ns stringer.core
  (:require [stringer.internal :as i]))


(defmacro with-obj-str
  "Given a single-binding vector (or a local symbol that's automatically bound to a new StringBuilder instance),
  execute the specified body of code in the binding context and return the string representation of the local."
  [bindings & body]
  (when-not (or (symbol? bindings) (vector? bindings))
    (throw (IllegalArgumentException.
             (str "Expected symbol or vector but found " (pr-str bindings)))))
  (when (and (vector? bindings)
          (not= 2 (count bindings)) (symbol? (first bindings)))
    (throw (IllegalArgumentException.
             (str "Expected single-binding vector but found " (pr-str bindings)))))
  (let [[local-symbol string-builder] (if (symbol? bindings)
                                        [(vary-meta bindings assoc :tag `java.lang.StringBuilder) '(StringBuilder.)]
                                        bindings)]
    `(let [~local-symbol ~string-builder]
       ~@body
       (.toString ~local-symbol))))


(defmacro append!
  "Append all arguments to the StringBuilder instance."
  [holder & args]
  (let [each-append (fn [x]
                      `(when-not (nil? ~x)  ; NULL doesn't work with StringBuilder
                         (.append ~holder ~x)))
        all-appends (->> args
                      (remove nil?)
                      i/precompile
                      (map each-append))]
    `(do ~@all-appends)))


(defmacro interleave!
 "Interleave args with delimiter and apply each element of the result sequence to the (.append holder %) method."
 [holder delimiter & args]
 `(append! ~holder ~@(interpose delimiter args)))


(defmacro join!
  "Like clojure.string/join, but appends tokens to a holder instead of returning a string."
  [holder delimiter coll]
  `(doseq [each# (interpose ~delimiter ~coll)]
     (append! ~holder each#)))


(defmacro strcat
  "Concatenate strings faster than 'str'. Note that this is a macro, hence
  cannot be used as a function."
  ([]
    "")
  ([token]
    (if (i/stringable? token)
      (str token)
      `(let [x# ~token]
         (if (nil? x#)
           ""
           (String/valueOf x#)))))
  ([token & more]
    (let [w (gensym)]
      `(with-obj-str [~(vary-meta w assoc :tag `java.lang.StringBuilder)
                      (StringBuilder. (strcat ~token))]
         (append! ~w ~@more)))))


(defmacro strdel
  "Concatenate tokens with specified delimiter."
  [delimiter & args]
  (if (i/stringable? delimiter)
    `(strcat ~@(doall (interpose delimiter args)))
    (let [delim (gensym)
          delimited-args (doall (interpose delim args))]
      `(let [~delim ~delimiter]
         (strcat
           ~@delimited-args)))))