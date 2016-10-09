;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns stringer.composite-perf-test
  (:require
    [clojure.test :refer :all]
    [clojure.string     :as t]
    [citius.core        :as c]
    [stringer.test-data :as d]
    [stringer.core      :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer large-tokens"
                       :chart-filename (format "bench-composite-clj-%s.png"
                                         c/clojure-version-str)}))


(deftest test-concat-large-text
  (testing "concatenating large text"
    (c/compare-perf "3 large tokens"
      (str d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)
      (stringer.core/strcat d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


(deftest test-largetext-join-perf
  (testing "joining large text"
    (c/compare-perf "3 large tokens"
      (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
      (stringer.core/strdel ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


(deftest test-format-large-text
  (testing "formatting large text"
    (c/compare-perf "3 large tokens"
      (format "%s, %s, %s" d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)
      (stringer.core/strfmt "%s, %s, %s" d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum))))


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


(deftest test-composite-concat
  (c/compare-perf "composite operations"
    (composite-str composite-data)
    (composite-strcat composite-data)))
