;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


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
