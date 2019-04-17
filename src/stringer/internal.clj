;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


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


(defn nparse
  "Parse given format-string containing named parameters,
  returning string and param-key tokens.

  Example: (nparse \"{name}-{age}\") ==> [:name \"-\" :age]"
  [template]
  (let [n           (count template)
        update-last (fn [tokens f] (update tokens (dec (count tokens)) f))
        suffix-last (fn [tokens suffix] (update-last tokens #(str % suffix)))
        update-many (fn [m kf-map] (reduce-kv (fn [m k v] (assoc m k (if (contains? kf-map k)
                                                                       ((get kf-map k) v)
                                                                       v)))
                                              {} m))]
    (loop [index 0
           state {:escape? false
                  :in-key? false
                  :tokens  [""]}]
      (if (>= index n)
        (:tokens state)
        (let [each (get template index)
              j    (unchecked-inc index)]
          (cond
            (:escape? state) (recur j (update-many state {:escape? (fn [_] false)
                                                          :tokens  #(suffix-last % each)}))
            (:in-key? state) (case each
                               \\ (throw (ex-info "Cannot use escape \\ inside a param name"
                                                  {:bad-char each}))
                               \} (recur j (update-many state {:in-key? (fn [_] false)
                                                               :tokens  #(-> %
                                                                             (update-last keyword)
                                                                             (conj ""))}))
                               (recur j (update state :tokens suffix-last each)))
            (= \{ each)      (recur j (update-many state {:in-key? (fn [_] true)
                                                          :tokens  #(conj % "")}))
            (= \\ each)      (recur j (assoc state :escape? true))
            :otherwise       (recur j (update state :tokens suffix-last each))))))))
