;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns stringer.strtbl-perf-test
  (:require
    [clojure.test :refer :all]
    [clojure.pprint :as pp]
    [citius.core    :as c]
    [stringer.core  :as s]))


(use-fixtures :once (c/make-bench-wrapper ["Clojure" "Stringer"]
                      {:chart-title "Stringer strtbl"
                       :chart-filename (format "bench-strtbl-clj-%s.png"
                                         c/clojure-version-str)}))


(def one-row
  [{:name "Eddy"    :age 34 :gender :male   :department :accounts   :active? false}])


(def ten-rows
  [{:name "Eddy"    :age 34 :gender :male   :department :accounts   :active? false}
   {:name "Heather" :age 42 :gender :female :department :production :active? true}
   {:name "Sanju"   :age 29 :gender :female :department :hr         :active? true}
   {:name "Rohit"   :age 23 :gender :male   :department :appdev     :active? true}
   {:name "Fred"    :age 52 :gender :male   :department :sales      :active? false}
   {:name "Henry"   :age 31 :gender :male   :department :accounts   :active? false}
   {:name "Laura"   :age 40 :gender :female :department :production :active? true}
   {:name "Susan"   :age 27 :gender :female :department :hr         :active? true}
   {:name "Bhavesh" :age 25 :gender :male   :department :appdev     :active? true}
   {:name "Peter"   :age 54 :gender :male   :department :sales      :active? false}])


(def twenty-rows
  (concat ten-rows ten-rows))


(deftest test-strtbl-str-perf
  (testing "str 1 row"
    (c/compare-perf "str-1row" (with-out-str (pp/print-table one-row)) (stringer.core/strtbl one-row)))
  (testing "str 10 rows"
    (c/compare-perf "str-10rows"
      (with-out-str (pp/print-table ten-rows)) (stringer.core/strtbl ten-rows)))
  (testing "str 20 rows"
    (c/compare-perf "str-20rows"
      (with-out-str (pp/print-table twenty-rows)) (stringer.core/strtbl twenty-rows))))


(deftest test-strtbl-out-str-perf
  (testing "out-str 1 row"
    (c/compare-perf "out-str-1row"
      (with-out-str (pp/print-table one-row))
      (with-out-str (println (stringer.core/strtbl one-row)))))
  (testing "out-str 10 rows"
    (c/compare-perf "out-str-10rows"
      (with-out-str (pp/print-table ten-rows))
      (with-out-str (println (stringer.core/strtbl ten-rows)))))
  (testing "out-str 20 rows"
    (c/compare-perf "out-str-20rows"
      (with-out-str (pp/print-table twenty-rows))
      (with-out-str (println (stringer.core/strtbl twenty-rows))))))
