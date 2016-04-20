(ns stringer.internal)


(defn expected
  ([expectation found]
    (throw (IllegalArgumentException.
             (format "Expected %s, but found (%s) %s" expectation (class found) (pr-str found)))))
  ([pred expectation found]
    (when-not (pred found)
      (expected expectation found))))


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
    (reduce (fn [x y]
              (if (stringable? y)
                (cond
                  (string? (last x)) (conj (pop x) (str (last x) y))
                  (empty? x)         (conj x (str y))
                  :otherwise         (conj x y))
                (conj x y)))
      [])))


(def ^String line-separator (System/getProperty "line.separator"))
