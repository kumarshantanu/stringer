;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


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
