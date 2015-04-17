(ns stringer.internal)


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
              (if (and (seq x) (string? (last x)) (stringable? y))
                (conj (pop x) (str (last x) y))
                (conj x y)))
      [])))


