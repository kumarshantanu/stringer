(ns stringer.core)


(defn stringable?
  [x]
  (not (or (symbol? x)
         (coll? x)
         (seq? x))))


(defn precompile
  "Given a collection, replace the continuous stringable elements as string and return the collection."
  [coll]
  (->> coll
    (remove nil?)
    (reduce (fn
              ([] (str))
              ([x] (str x))
              ([x y] (if (and (seq x) (stringable? y))
                       (conj (pop x) (str (last x) y))
                       (conj x y))))
      [])))


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
                      precompile
                      (map each-append))]
    `(do ~@all-appends)))


(defmacro join!
 "Interleave args with delimiter and apply each element of the result sequence to the (.append holder %) method."
 [holder delimiter & args]
 `(append! ~holder ~@(interpose delimiter args)))


(defmacro strcat
  "Concatenate strings faster than 'str'. Note that this is a macro, hence
  cannot be used as a function."
  [& args]
  (let [args (precompile args)]
    (cond
      (empty? args)          ""
      (= 1 (count args))     (if (stringable? (first args))
                               (str (first args))
                               `(let [x# ~(first args)]
                                  (if (nil? x#)
                                    ""
                                    (String/valueOf x#))))
      (string? (first args)) (let [w (gensym)]
                               `(with-obj-str [~(vary-meta w assoc :tag `java.lang.StringBuilder)
                                               (StringBuilder. ~(first args))]
                                  (append! ~w ~@(rest args))))
      (symbol? (first args)) (let [x (gensym)
                                   y (vary-meta (gensym) assoc :tag `java.lang.String)
                                   more (rest args)
                                   w (gensym)]
                               `(let [~x ~(first args)]
                                  (if (string? ~x)
                                    (let [~y ~x]
                                      (with-obj-str [~(vary-meta w assoc :tag `java.lang.StringBuilder)
                                                     (StringBuilder. ~y)]
                                        (append! ~w ~@more)))
                                    (with-obj-str w#
                                      (append! w# ~@args)))))
      :otherwise             `(with-obj-str w#
                                (append! w# ~@args)))))


(defmacro strjoin
  "Concatenate tokens with specified delimiter."
  [delimiter & args]
  (let [delim (gensym)]
    `(let [~delim ~delimiter]
       (strcat-impl
         ~@(interpose delim args)))))
