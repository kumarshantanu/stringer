;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns stringer.core-test
  (:require
    [clojure.test :refer :all]
    [clojure.pprint :as pp]
    [clojure.string :as string]
    [stringer.core  :as s]))


(deftest test-with-obj-str
  (is (= "[1, 2, 3]"
        (s/with-obj-str [w (java.util.ArrayList.)]
          (.add w 1)
          (.add w 2)
          (.add w 3))))
  (is (= "foobarbaz"
        (s/with-obj-str w
          (.append w "foo")
          (.append w "bar")
          (.append w "baz")))))


(deftest test-append!
  (is (= "39false\nfoo"
        (s/with-obj-str w
          (s/append! w 39 false \newline "foo" nil))))
  (is (= "39false\nfoo"
        (s/with-obj-str [w (StringBuffer.)]
          (s/append! w 39 false \newline "foo" nil)))))


(deftest test-join!
  (is (= "1, 2, 3foo|:bar"
        (s/with-obj-str w
          (s/join-all! w ", " 1 2 3)
          (s/join! w \| ["foo" :bar]))))
  (is (= "foo:1, 2, 3bar|:baz"
        (s/with-obj-str w
          (s/append! w "foo:")
          (s/join-all! w ", " 1 2 3)
          (s/join! w \| ["bar" :baz])))))


(deftest test-strcat
  (testing "no-arg"
    (is (= (str) (s/strcat)))))


(deftest test-strdel
  (testing "no-arg"
    (is (= (clojure.string/join ", " []) (stringer.core/strdel ", "))))
  (testing "one-arg"
    (is (= (clojure.string/join ", " [1]) (stringer.core/strdel ", " 1)))
    (let [one 1]
      (is (= (clojure.string/join ", " [one]) (stringer.core/strdel ", " one)))))
  (testing "multi-args"
    (is (= (clojure.string/join ", " [1 2 3]) (stringer.core/strdel ", " 1 2 3)))
    (let [one 1
          two 2
          three 3]
      (is (= (clojure.string/join ", " [one two three]) (stringer.core/strdel ", " one two three)))))
  (testing "various-args"
    (is (=
          (clojure.string/join ", " [1 :er nil \newline false "foo"])
          (stringer.core/strdel ", " 1 :er nil \newline false "foo")))
    (let [one 1
          er-keyword :er
          null nil
          newline-char \newline
          false-bool false
          foo-str "foo"]
      (is (=
            (clojure.string/join ", " [one er-keyword null newline-char false-bool foo-str])
            (stringer.core/strdel ", " one er-keyword ^Object null newline-char false-bool foo-str))))))


(def fmt "%s")


(deftest test-strfmt
  (testing "no-arg"
    (is (= (format "") (stringer.core/strfmt "")))
    (is (= (format "foo bar") (stringer.core/strfmt "foo bar"))))
  (testing "one-arg"
    ;; placement
    (is (= (format "%s bar baz" "foo") (stringer.core/strfmt "%s bar baz" "foo")))
    (is (= (format "foo %s baz" "bar") (stringer.core/strfmt "foo %s baz" "bar")))
    (is (= (format "foo bar %s" "baz") (stringer.core/strfmt "foo bar %s" "baz")))
    (let [x "baz"]
      (is (= (format "foo bar %s" x) (stringer.core/strfmt "foo bar %s" x))))
    ;; boolean
    (is (= (format "%b" false) (stringer.core/strfmt "%b" false)))
    (is (= (format "%b" nil)   (stringer.core/strfmt "%b" nil)))
    (is (= (format "%b" :foo)  (stringer.core/strfmt "%b" :foo)))
    (is (= (format "%B" :foo)  (stringer.core/strfmt "%B" :foo)))
    (is (= (format "%B" nil)   (stringer.core/strfmt "%B" nil)))
    ;; integer
    (is (= (format "%d" 45)    (stringer.core/strfmt "%d" 45)))
    (is (= (format "%d" nil)   (stringer.core/strfmt "%d" nil)))
    ;; float
    (is (= (format "%f" 45.67) (stringer.core/strfmt "%f" 45.67)))
    (is (= (format "%f"
                   45.6789012) (stringer.core/strfmt "%f" 45.6789012)))
    (is (= (format "%f" nil)   (stringer.core/strfmt "%f" nil)))
    ;; hash code
    (is (= (format "%h" :foo)  (stringer.core/strfmt "%h" :foo)))
    (is (= (format "%h" nil)   (stringer.core/strfmt "%h" nil)))
    (is (= (format "%H" :foo)  (stringer.core/strfmt "%H" :foo)))
    (is (= (format "%H" nil)   (stringer.core/strfmt "%H" nil)))
    ;; platform-specific newline
    (is (= (format "%n")       (stringer.core/strfmt "%n")))
    (is (= (format "%n")       (stringer.core/strfmt "%n")))
    ;; octal
    (is (= (format "%o" 45)    (stringer.core/strfmt "%o" 45)))
    (is (= (format "%o" nil)   (stringer.core/strfmt "%o" nil)))
    ;; string
    (is (= (format "%s" :foo)  (stringer.core/strfmt "%s" :foo)))
    (is (= (format "%s" nil)   (stringer.core/strfmt "%s" nil)))
    (is (= (format "%S" :foo)  (stringer.core/strfmt "%S" :foo)))
    (is (= (format "%S" nil)   (stringer.core/strfmt "%S" nil)))
    ;; hex
    (is (= (format "%x" 1234)  (stringer.core/strfmt "%x" 1234)))
    (is (= (format "%x" nil)   (stringer.core/strfmt "%x" nil)))
    (is (= (format "%X" 1234)  (stringer.core/strfmt "%X" 1234)))
    (is (= (format "%X" nil)   (stringer.core/strfmt "%X" nil)))
    ;; var
    (is (= (format fmt "foo")  (stringer.core/strfmt fmt "foo"))))
  (testing "multi-args"
    (is (= (format "Customer: %s, Orders: %d for this month." "XYZ Corp" 13)
          (stringer.core/strfmt "Customer: %s, Orders: %d for this month." "XYZ Corp" 13)))))


(deftest test-repchar
  (testing "happy cases"
    (is (= (string/join (repeat 0 \-)) (stringer.core/repchar 0 \-)))
    (is (= (string/join (repeat -4 \-)) (stringer.core/repchar -4 \-)))
    (is (= (string/join (repeat 10 \-)) (stringer.core/repchar 10 \-))))
  (testing "bad input"
    (is (thrown? NullPointerException
          (stringer.core/repchar 10 nil)))
    (is (thrown? ClassCastException
          (stringer.core/repchar 10 :foo)))))


(deftest test-repstr
  (testing "happy cases"
    (is (= (string/join (repeat 0 :foo)) (stringer.core/repstr 0 :foo)))
    (is (= (string/join (repeat -4 :foo)) (stringer.core/repstr -4 :foo)))
    (is (= (string/join (repeat 10 nil)) (stringer.core/repstr 10 nil)))
    (is (= (string/join (repeat 10 "hey")) (stringer.core/repstr 10 "hey")))))


(def data
  [{:name "Eddy"    :age 34 :gender :male   :department :accounts   :active? false}
   {:name "Heather" :age 42 :gender :female :department :production :active? true}
   {:name "Sanju"   :age 29 :gender :female :department :hr         :active? true}
   {:name "Rohit"   :age 23 :gender :male   :department :appdev     :active? true}
   {:name "Fred"    :age 52 :gender :male   :department :sales      :active? false}])


(deftest test-strtbl
  (is (= (with-out-str (pp/print-table data)) (with-out-str (println (stringer.core/strtbl data))))))


(deftest test-nformat
  (testing "named params should be picked from local vars in arity-0"
    (let [name "Harry"
          place "Azkaban"]
      (is (= "Hi Harry, are you from Azkaban?"
             (stringer.core/nformat "Hi {name}, are you from {place}?"))
          "regular param values"))
    (let [^Object name nil
          ^Object place nil]
      (is (= "Hi , are you from ?"
             (stringer.core/nformat "Hi {name}, are you from {place}?"))
          "nil param values")))
  (testing "named params should be picked from map argument"
    (is (= "Hi Harry, are you from Azkaban?"
           (stringer.core/nformat "Hi {name}, are you from {place}?" {:name "Harry" :place "Azkaban"})
           (let [format-string-lv "Hi {name}, are you from {place}?"]
             (stringer.core/nrender format-string-lv {:name "Harry" :place "Azkaban"})))
        "regular param values")
    (is (= "Hi , are you from ?"
           (stringer.core/nformat "Hi {name}, are you from {place}?" {:name nil :place nil})
           (let [format-string-lv "Hi {name}, are you from {place}?"]
             (stringer.core/nrender format-string-lv {:name nil :place nil})))
        "nil param values")))


(def fmt-str "Hi {name}, are you from {place}?")


(deftest test-fmt
  (let [f1 (stringer.core/fmt "Hi {name}, are you from {place}?")
        f2 (stringer.core/fmt fmt-str)]
    (is (= "Hi Harry, are you from Azkaban?"
           (f1 {:name "Harry" :place "Azkaban"})))
    (is (= "Hi Harry, are you from Azkaban?"
           (f2 {:name "Harry" :place "Azkaban"})))))
