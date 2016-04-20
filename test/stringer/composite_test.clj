(ns stringer.composite-test
  (:require
    [clojure.test :refer :all]
    [clojure.string     :as t]
    [stringer.test-data :as d]
    [stringer.core      :as s]))


(deftest test-largetext-join
  (testing "large-text"
    (is (=
          (clojure.string/join ", " [d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum])
          (stringer.core/strdel ", " d/lorem-ipsum d/lorem-ipsum d/lorem-ipsum)))))
