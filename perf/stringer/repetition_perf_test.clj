(ns stringer.repetition-perf-test
  (:require
    [clojure.test :refer :all]
    [citius.core    :as c]
    [clojure.string :as string]
    [stringer.core  :as s]))


(use-fixtures :once (c/make-bench-wrapper ["apply str" "clojure.string/join" "Stringer"]
                      {:chart-title "Stringer repetition"
                       :chart-filename (format "bench-repetition-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-repchar-perf
  (testing "one time"
    (c/compare-perf "repchar-1" (apply str (repeat 1 \x)) (string/join (repeat 1 \x)) (stringer.core/repchar 1 \x)))
  (testing "multiple times"
    (c/compare-perf "repchar-10"
      (apply str (repeat 10 \x)) (string/join (repeat 10 \x)) (stringer.core/repchar 10 \x))
    (c/compare-perf "repchar-100"
      (apply str (repeat 100 \x)) (string/join (repeat 100 \x)) (stringer.core/repchar 100 \x))))


(deftest test-repstr-perf
  (testing "one time"
    (c/compare-perf "repstr-1"
      (apply str (repeat 1 "foo")) (string/join (repeat 1 "foo")) (stringer.core/repstr 1 "foo")))
  (testing "multiple times"
    (c/compare-perf "repstr-10"
      (apply str (repeat 10 "this is a string"))
      (string/join (repeat 10 "this is a string"))
      (stringer.core/repstr 10 "this is a string"))
    (c/compare-perf "repstr-100"
      (apply str (repeat 100 "this is a longer string"))
      (string/join (repeat 100 "this is a longer string"))
      (stringer.core/repstr 100 "this is a longer string"))))
