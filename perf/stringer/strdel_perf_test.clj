(ns stringer.strdel-perf-test
  (:require
    [clojure.test :refer :all]
    [citius.core   :as c]
    [stringer.core :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer strdel"
                       :chart-filename (format "bench-strdel-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-strdel-perf
  (testing "one-arg"
    (c/compare-perf "1 literal" (clojure.string/join ", " [1]) (stringer.core/strdel ", " 1))
    (let [one 1]
      (c/compare-perf "1 local" (clojure.string/join ", " [one]) (stringer.core/strdel ", " one))))
  (testing "multi-args"
    (c/compare-perf "3 literals" (clojure.string/join ", " [1 2 3]) (stringer.core/strdel ", " 1 2 3))
    (let [one 1
          two 2
          three 3]
      (c/compare-perf "3 locals"
        (clojure.string/join ", " [one two three])
        (stringer.core/strdel ", " one two three))))
  (testing "various-args"
    (c/compare-perf "6 literals"
      (clojure.string/join ", " [1 :er nil \newline false "foo"])
      (stringer.core/strdel ", " 1 :er nil \newline false "foo"))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (c/compare-perf "6 locals"
        (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
        (stringer.core/strdel ", " one er-keyword ^Object null newline-char false-bool foo-str)))))
