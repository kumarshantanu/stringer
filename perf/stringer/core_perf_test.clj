(ns stringer.core-perf-test
  (:require
    [clojure.test :refer :all]
    [citius.core   :as c]
    [stringer.core :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer small-tokens"
                       :chart-filename (format "bench-small-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-strcat-perf
  (testing "one-arg"
    (c/compare-perf "+ 1 literal" (str 34) (stringer.core/strcat 34))
    (let [thirty-four 34]
      (c/compare-perf "+ 1 local" (str thirty-four) (stringer.core/strcat thirty-four))))
  (testing "small tokens"
    (c/compare-perf "+ 3 str literals" (str "foo" "bar" "baz") (stringer.core/strcat "foo" "bar" "baz"))
    (let [foo "foo" bar "bar" baz "baz"]
      (c/compare-perf "+ 3 locals" (str foo bar baz) (stringer.core/strcat foo bar baz))))
  (testing "various tokens"
    (c/compare-perf "+ 5 literals" (str 34 :er nil \- "foo") (stringer.core/strcat 34 :er nil \- "foo"))
    (let [thirty-four 34
          er-keyword :er
          null nil
          dash-char \-
          foo-str "foo"]
      (c/compare-perf "+ 5 locals"
        (str thirty-four er-keyword null dash-char foo-str)
        (stringer.core/strcat thirty-four er-keyword ^Object null dash-char foo-str)))))


(deftest test-strdel-perf
  (testing "one-arg"
    (c/compare-perf "| 1 literal" (clojure.string/join ", " [1]) (stringer.core/strdel ", " 1))
    (let [one 1]
      (c/compare-perf "| 1 local" (clojure.string/join ", " [one]) (stringer.core/strdel ", " one))))
  (testing "multi-args"
    (c/compare-perf "| 3 literals" (clojure.string/join ", " [1 2 3]) (stringer.core/strdel ", " 1 2 3))
    (let [one 1
          two 2
          three 3]
      (c/compare-perf "| 3 locals" (clojure.string/join ", " [one two three]) (stringer.core/strdel ", " one two three))))
  (testing "various-args"
    (c/compare-perf "| 6 literals"
      (clojure.string/join ", " [1 :er nil \newline false "foo"])
      (stringer.core/strdel ", " 1 :er nil \newline false "foo"))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (c/compare-perf "| 6 locals"
        (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
        (stringer.core/strdel ", " one er-keyword ^Object null newline-char false-bool foo-str)))))
