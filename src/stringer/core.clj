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
                      `(let [x# ~x]
                         (when-not (nil? x#)  ; NULL doesn't work with StringBuilder
                           (.append ~holder x#))))
        all-appends (->> args
                      (remove nil?)
                      i/precompile
                      (map each-append))]
    `(do ~@all-appends)))


(defmacro join-all!
 "Interpose args with delimiter and apply each element of the result sequence to the (.append holder %) method."
 [holder delimiter & args]
 `(append! ~holder ~@(interpose delimiter args)))


(defmacro join!
  "Like clojure.string/join, but appends tokens to a holder instead of returning a string."
  [holder delimiter coll]
  `(loop [coll# (seq ~coll)
          del?# false]
     (when coll#
       (when del?#
         (append! ~holder ~delimiter))
       (append! ~holder (first coll#))
       (recur (next coll#) true))))


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


(defmacro strfmt
  "Format (like clojure.core/format but only %d, %f, %s supported) given string literal with arguments."
  [format-str & args]
  (i/expected string? "first argument to 'strfmt' to be string literal" format-str)
  (let [sb (gensym "sb-")
        conj-str (fn [exps buf]
                   (if (seq buf)
                     (let [^String s (apply str buf)]
                       (conj exps `(.append ~sb ~s)))
                     exps))
        head-arg (fn [arg-seq]
                   (if (seq arg-seq)
                     (first arg-seq)
                     (throw (IllegalArgumentException. "Insufficient arguments to 'strfmt'"))))]
    (loop [fmt  format-str ; remaining format string to process
           buf  []         ; buffer for the current contiguous string
           args args       ; remaining args to process
           exps []]        ; expressions to evaluate later
      (if (empty? fmt)
        `(with-obj-str [~sb (StringBuilder. ~(count format-str))]
           ~@(conj-str exps buf))
        (let [ch (first fmt)
              c2 (second fmt)]
          (if (= ch \%)
            (case c2
              \%  (recur (subs fmt 2) (conj buf c2) args exps)
              \d  (recur (subs fmt 2) [] (next args) (-> exps
                                                       (conj-str buf)
                                                       (conj `(.append ~sb (long ~(head-arg args))))))
              \f  (recur (subs fmt 2) [] (next args) (-> exps
                                                       (conj-str buf)
                                                       (conj `(.append ~sb (double ~(head-arg args))))))
              \s  (recur (subs fmt 2) [] (next args) (-> exps
                                                       (conj-str buf)
                                                       (conj `(.append ~sb ~(head-arg args)))))
              nil (i/expected "either %d, %f or %s" "%")
              (i/expected "either %s, %d or %f" (str \% c2)))
            (recur (subs fmt 1) (conj buf ch) args exps)))))))
