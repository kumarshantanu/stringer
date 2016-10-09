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


(deftest test-strtbl-perf
  (testing "1 row"
    (c/compare-perf "1 row" (with-out-str (pp/print-table one-row)) (stringer.core/strtbl one-row)))
  (testing "10 rows"
    (c/compare-perf "10 rows"
      (with-out-str (pp/print-table ten-rows)) (stringer.core/strtbl ten-rows)))
  (testing "20 rows"
    (c/compare-perf "20 rows"
      (with-out-str (pp/print-table twenty-rows)) (stringer.core/strtbl twenty-rows))))
