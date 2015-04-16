(ns stringer.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint     :as pp]
            [clojure.string     :as t]
            [stringer.core      :as s]
            [stringer.test-data :as d]
            [criterium.core     :as c]))


(defmacro measure
  [expr]
  `(do
     (println "\n::::: Benchmarking" ~(pr-str expr))
     (let [result# (c/benchmark ~expr {})]
       [result# (with-out-str (c/report-result result#))])))


(defmacro compare-perf
  [slow-expr fast-expr]
  `(do
     (is (= ~slow-expr ~fast-expr))
     (let [[slow-bench# slow-report#] (measure ~slow-expr)
           [fast-bench# fast-report#] (measure ~fast-expr)]
       (let [slow-label# ~(pr-str slow-expr)
             fast-label# ~(pr-str fast-expr)]
         (->> [slow-report# fast-report#]
           (map t/split-lines)
           (apply map (fn [s# f# ] {slow-label# s# fast-label# f#}))
           (pp/print-table [slow-label# fast-label#])))
       (is (>= (first (:mean slow-bench#))
             (first (:mean fast-bench#)))))))


(deftest test-with-obj-str
  (is (= "[1, 2, 3]"
        (s/with-obj-str [w (java.util.ArrayList.)]
          (.add w 1)
          (.add w 2)
          (.add w 3))))
  (is (= "foobarbaz"
        (s/with-obj-str w
          (.append w "foo")
          (.append w "bar")
          (.append w "baz")))))


(deftest test-append!
  (is (= "39false\nfoo"
        (s/with-obj-str w
          (s/append! w 39 false \newline "foo" nil))))
  (is (= "39false\nfoo"
        (s/with-obj-str [w (StringBuffer.)]
          (s/append! w 39 false \newline "foo" nil)))))


(deftest test-join!
  (is (= "1, 2, 3"
        (s/with-obj-str w
          (s/join! w ", " 1 2 3))))
  (is (= "foo:1, 2, 3"
        (s/with-obj-str w
          (s/append! w "foo:")
          (s/join! w ", " 1 2 3)))))


(deftest ^{:perf true :strcat true} test-strcat
  (testing "no-arg"
    (compare-perf (str) (s/strcat)))
  (testing "one-arg"
    (compare-perf (str 34) (s/strcat 34)))
  (testing "small tokens"
    (compare-perf (str "foo" "bar" "baz") (s/strcat "foo" "bar" "baz")))
  (testing "various tokens"
    (compare-perf (str 34 :er nil \- "foo") (s/strcat 34 :er nil \- "foo")))
  (testing "large text"
    (compare-perf
      (str d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)
      (s/strcat d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


(deftest ^{:perf true :strjoin true} test-strjoin
  (testing "no-arg"
    (compare-perf (t/join ", " []) (s/strjoin ", ")))
  (testing "one-arg"
    (compare-perf (t/join ", " [1]) (s/strjoin ", " 1)))
  (testing "multi-args"
    (compare-perf (t/join ", " [1 2 3]) (s/strjoin ", " 1 2 3)))
  (testing "various-args"
    (compare-perf (t/join ", " [1 :er nil \newline false "foo"]) (s/strjoin ", " 1 :er nil \newline false "foo")))
  (testing "large-text"
    (compare-perf (t/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
      (s/strjoin ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))
