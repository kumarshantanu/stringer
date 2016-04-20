(ns stringer.strfmt-perf-test
  (:require
    [clojure.test :refer :all]
    [citius.core   :as c]
    [stringer.core :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer strfmt"
                       :chart-filename (format "bench-strfmt-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-strfmt-perf
  (testing "one-arg"
    (c/compare-perf "1 literal" (format "foo %s" "bar") (stringer.core/strfmt "foo %s" "bar"))
    (let [foo "foo"]
      (c/compare-perf "1 local" (format "%s bar" foo) (stringer.core/strfmt "%s bar" foo))))
  (testing "multi-args"
    (c/compare-perf "3 literals"
      (format "foo %s, %d: %f" "bar" 42 4.2)
      (stringer.core/strfmt "foo %s, %d: %f" "bar" 42 4.2))
    (let [bar "bar"
          baz 42
          qux 4.2]
      (c/compare-perf "3 locals"
      (format "foo %s, %d: %f" bar baz qux)
      (stringer.core/strfmt "foo %s, %d: %f" bar baz qux)))))
