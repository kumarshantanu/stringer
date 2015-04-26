(ns stringer.core-test
  (:require
    [clojure.test :refer :all]
    [stringer.core         :as s]
    [stringer.test-harness :as h]))


(use-fixtures :once (h/make-bench-test-wrapper "bench-small.png"))


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


(deftest test-strcat
  (testing "no-arg"
    (is (= (str) (s/strcat)))))


(deftest ^{:perf true :strcat true} test-strcat-perf
  (testing "one-arg"
    (h/compare-perf (str 34) (stringer.core/strcat 34))
    (let [thirty-four 34]
      (h/compare-perf (str thirty-four) (stringer.core/strcat thirty-four))))
  (testing "small tokens"
    (h/compare-perf (str "foo" "bar" "baz") (stringer.core/strcat "foo" "bar" "baz"))
    (let [foo "foo" bar "bar" baz "baz"]
      (h/compare-perf (str foo bar baz) (stringer.core/strcat foo bar baz))))
  (testing "various tokens"
    (h/compare-perf (str 34 :er nil \- "foo") (stringer.core/strcat 34 :er nil \- "foo"))
    (let [thirty-four 34
          er-keyword :er
          null nil
          dash-char \-
          foo-str "foo"]
      (h/compare-perf
        (str thirty-four er-keyword null dash-char foo-str)
        (stringer.core/strcat thirty-four er-keyword ^Object null dash-char foo-str)))))


(deftest test-strjoin
  (testing "no-arg"
    (is (= (clojure.string/join ", " []) (stringer.core/strjoin ", "))))
  (testing "one-arg"
    (is (= (clojure.string/join ", " [1]) (stringer.core/strjoin ", " 1)))
    (let [one 1]
      (is (= (clojure.string/join ", " [one]) (stringer.core/strjoin ", " one)))))
  (testing "multi-args"
    (is (= (clojure.string/join ", " [1 2 3]) (stringer.core/strjoin ", " 1 2 3)))
    (let [one 1
          two 2
          three 3]
      (is (= (clojure.string/join ", " [one two three]) (stringer.core/strjoin ", " one two three)))))
  (testing "various-args"
    (is (=
          (clojure.string/join ", " [1 :er nil \newline false "foo"])
          (stringer.core/strjoin ", " 1 :er nil \newline false "foo")))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (is (=
            (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
            (stringer.core/strjoin ", " one er-keyword ^Object null newline-char false-bool foo-str))))))


(deftest ^{:perf true :strjoin true} test-strjoin-perf
  (testing "one-arg"
    (h/compare-perf (clojure.string/join ", " [1]) (stringer.core/strjoin ", " 1))
    (let [one 1]
      (h/compare-perf (clojure.string/join ", " [one]) (stringer.core/strjoin ", " one))))
  (testing "multi-args"
    (h/compare-perf (clojure.string/join ", " [1 2 3]) (stringer.core/strjoin ", " 1 2 3))
    (let [one 1
          two 2
          three 3]
      (h/compare-perf (clojure.string/join ", " [one two three]) (stringer.core/strjoin ", " one two three))))
  (testing "various-args"
    (h/compare-perf
      (clojure.string/join ", " [1 :er nil \newline false "foo"])
      (stringer.core/strjoin ", " 1 :er nil \newline false "foo"))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (h/compare-perf
        (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
        (stringer.core/strjoin ", " one er-keyword ^Object null newline-char false-bool foo-str)))))
