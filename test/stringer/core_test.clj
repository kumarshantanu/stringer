(ns stringer.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint     :as pp]
            [clojure.string     :as t]
            [stringer.core      :as s]
            [stringer.test-data :as d]
            [criterium.core     :as c]
            [clansi.core        :as a]))


(defmacro measure
  [expr]
  `(do
     (println "\n::::: Benchmarking" ~(pr-str expr))
     (let [result# (c/benchmark ~expr {})]
       [result# (with-out-str (c/report-result result#))])))


(defn nix?
  []
  (let [os (t/lower-case (str (System/getProperty "os.name")))]
    (some #(>= (.indexOf os ^String %) 0) ["mac" "linux" "unix"])))


(defn colorize
  [text & args]
  (if (nix?)
    (apply a/style text args)
    text))


(defn comparison-summary
  [slow-bench fast-bench]
  (let [slow-mean (first (:mean slow-bench))
        fast-mean (first (:mean fast-bench))
        diff (Math/abs ^double (- slow-mean fast-mean))
        calc-percent (fn [] (double (/ (* 100 diff)
                                      (if (> slow-mean fast-mean)
                                        fast-mean
                                        slow-mean))))]
    (cond
      (= slow-mean fast-mean) (colorize "Both are equal" :yellow)
      (> slow-mean fast-mean) (colorize (format "Stringer is faster by %.2f%%" (calc-percent) \%) :bg-white)
      :otherwise              (colorize (format "Stringer is slower by %.2f%%" (calc-percent) \%) :bg-red))))


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
           (apply map (fn [s# f# ] {slow-label# s#
                                    fast-label# f#}))
           (pp/print-table [slow-label# fast-label#])))
       (println (comparison-summary slow-bench# fast-bench#))
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


(deftest test-strcat
  (testing "no-arg"
    (is (= (str) (s/strcat)))))


(deftest ^{:perf true :strcat true} test-strcat-perf
  (testing "one-arg"
    (compare-perf (str 34) (stringer.core/strcat 34))
    (let [thirty-four 34]
      (compare-perf (str thirty-four) (stringer.core/strcat thirty-four))))
  (testing "small tokens"
    (compare-perf (str "foo" "bar" "baz") (stringer.core/strcat "foo" "bar" "baz"))
    (let [foo "foo" bar "bar" baz "baz"]
      (compare-perf (str foo bar baz) (stringer.core/strcat foo bar baz))))
  (testing "various tokens"
    (compare-perf (str 34 :er nil \- "foo") (stringer.core/strcat 34 :er nil \- "foo"))
    (let [thirty-four 34
          er-keyword :er
          null nil
          dash-char \-
          foo-str "foo"]
      (compare-perf
        (str thirty-four er-keyword null dash-char foo-str)
        (s/strcat thirty-four er-keyword ^Object null dash-char foo-str))))
  (testing "large text"
    (compare-perf
      (str d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)
      (s/strcat d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


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
            (stringer.core/strjoin ", " one er-keyword ^Object null newline-char false-bool foo-str)))))
  (testing "large-text"
    (is (=
          (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
          (stringer.core/strjoin ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)))))


(deftest ^{:perf true :strjoin true} test-strjoin
  (testing "one-arg"
    (compare-perf (clojure.string/join ", " [1]) (stringer.core/strjoin ", " 1))
    (let [one 1]
      (compare-perf (clojure.string/join ", " [one]) (stringer.core/strjoin ", " one))))
  (testing "multi-args"
    (compare-perf (clojure.string/join ", " [1 2 3]) (stringer.core/strjoin ", " 1 2 3))
    (let [one 1
          two 2
          three 3]
      (compare-perf (clojure.string/join ", " [one two three]) (stringer.core/strjoin ", " one two three))))
  (testing "various-args"
    (compare-perf
      (clojure.string/join ", " [1 :er nil \newline false "foo"])
      (stringer.core/strjoin ", " 1 :er nil \newline false "foo"))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (compare-perf
        (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
        (stringer.core/strjoin ", " one er-keyword ^Object null newline-char false-bool foo-str))))
  (testing "large-text"
    (compare-perf
      (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
      (stringer.core/strjoin ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))
