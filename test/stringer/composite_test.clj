(ns stringer.composite-test
  (:require
    [clojure.test :refer :all]
    [clojure.string        :as t]
    [stringer.core         :as s]
    [stringer.test-data    :as d]
    [stringer.test-harness :as h]))


(use-fixtures :once (h/make-bench-test-wrapper "bench-composite.png"))


(deftest ^{:perf true :strcat true} test-concat-large-text
  (testing "large text"
    (h/compare-perf
      (str d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)
      (stringer.core/strcat d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


(deftest test-largetext-join
  (testing "large-text"
    (is (=
          (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
          (stringer.core/strdel ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)))))


(deftest ^{:perf true :strdel true} test-largetext-join-perf
  (testing "large-text"
    (h/compare-perf
      (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
      (stringer.core/strdel ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


(defn composite-str
  [m]
  (t/join ", " [(str "Foo " (:foo m))
                (str "Bands: " (t/join ", " (:bar m)))
                (str "Diff: " (- ^long (get-in m [:baz :norf])
                                ^long (get-in m [:baz :quux])))]))


(defn composite-strcat
  [m]
  (s/with-obj-str w
    (s/append! w
      "Foo " (:foo m)
      ", "
      "Bands: " (t/join ", " (:bar m))
      ", "
      "Diff: " (- ^long (get-in m [:baz :norf])
                 ^long (get-in m [:baz :quux])))))


(def composite-data {:foo "fighters"
                     :bar ["U2" "MLTR"]
                     :baz {:quux 10
                           :norf 20}})


(deftest ^{:perf true :strdel true} test-composite-concat
  (h/compare-perf
    (composite-str composite-data)
    (composite-strcat composite-data)))
