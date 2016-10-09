;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns stringer.strcat-perf-test
  (:require
    [clojure.test :refer :all]
    [citius.core   :as c]
    [stringer.core :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer strcat"
                       :chart-filename (format "bench-strcat-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-strcat-perf
  (testing "one-arg"
    (c/compare-perf "1 literal" (str 34) (stringer.core/strcat 34))
    (let [thirty-four 34]
      (c/compare-perf "1 local" (str thirty-four) (stringer.core/strcat thirty-four))))
  (testing "small tokens"
    (c/compare-perf "3 str literals" (str "foo" "bar" "baz") (stringer.core/strcat "foo" "bar" "baz"))
    (let [foo "foo" bar "bar" baz "baz"]
      (c/compare-perf "3 locals" (str foo bar baz) (stringer.core/strcat foo bar baz))))
  (testing "various tokens"
    (c/compare-perf "5 literals" (str 34 :er nil \- "foo") (stringer.core/strcat 34 :er nil \- "foo"))
    (let [thirty-four 34
          er-keyword :er
          null nil
          dash-char \-
          foo-str "foo"]
      (c/compare-perf "5 locals"
        (str thirty-four er-keyword null dash-char foo-str)
        (stringer.core/strcat thirty-four er-keyword ^Object null dash-char foo-str)))))
