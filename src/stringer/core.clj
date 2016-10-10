;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns stringer.core
  (:require [stringer.internal :as i]))


(defmacro with-obj-str
  "Given a single-binding vector (or a local symbol that's automatically bound to a new StringBuilder instance),
  execute the specified body of code in the binding context and return the string representation of the local."
  [bindings & body]
  (when-not (or (symbol? bindings) (vector? bindings))
    (throw (IllegalArgumentException.
             (str "Expected symbol or vector but found " (pr-str bindings)))))
  (when (and (vector? bindings)
          (not= 2 (count bindings)) (symbol? (first bindings)))
    (throw (IllegalArgumentException.
             (str "Expected single-binding vector but found " (pr-str bindings)))))
  (let [[local-symbol string-builder] (if (symbol? bindings)
                                        [(vary-meta bindings assoc :tag `java.lang.StringBuilder) '(StringBuilder.)]
                                        bindings)]
    `(let [~local-symbol ~string-builder]
       ~@body
       (.toString ~local-symbol))))


(defmacro append!
  "Append all arguments to the StringBuilder instance."
  [holder & args]
  (let [each-append (fn [x]
                      `(let [x# ~x]
                         (when-not (nil? x#)  ; NULL doesn't work with StringBuilder
                           (.append ~holder x#))))
        all-appends (->> args
                      (remove nil?)
                      i/precompile
                      (map each-append))]
    `(do ~@all-appends)))


(defmacro join-all!
 "Interpose args with delimiter and apply each element of the result sequence to the (.append holder %) method."
 [holder delimiter & args]
 `(append! ~holder ~@(interpose delimiter args)))


(defmacro join!
  "Like clojure.string/join, but appends tokens to a holder instead of returning a string."
  [holder delimiter coll]
  `(loop [coll# (seq ~coll)
          del?# false]
     (when coll#
       (when del?#
         (append! ~holder ~delimiter))
       (append! ~holder (first coll#))
       (recur (next coll#) true))))


(defmacro strcat
  "Concatenate strings faster than 'str'. Note that this is a macro, hence
  cannot be used as a function."
  ([]
    "")
  ([token]
    (if (i/stringable? token)
      (str token)
      `(let [x# ~token]
         (if (nil? x#)
           ""
           (String/valueOf x#)))))
  ([token & more]
    (let [w (gensym)]
      `(with-obj-str [~(vary-meta w assoc :tag `java.lang.StringBuilder)
                      (StringBuilder. (strcat ~token))]
         (append! ~w ~@more)))))


(defmacro strdel
  "Concatenate tokens with specified delimiter."
  [delimiter & args]
  (if (i/stringable? delimiter)
    `(strcat ~@(doall (interpose delimiter args)))
    (let [delim (gensym)
          delimited-args (doall (interpose delim args))]
      `(let [~delim ~delimiter]
         (strcat
           ~@delimited-args)))))


(defmacro strfmt
  "Format (like clojure.core/format but only %b, %d, %f, %h, %n, %o, %s, %x supported) given string with arguments. The
  format-string must resolve as a string at compile time. Flags and width/precision specifiers are not supported."
  [format-str & args]
  (let [format-str (let [fmts (eval format-str)]
                     (i/expected string? "first argument to 'strfmt' to be string at compile time" fmts)
                     fmts)
        sb (gensym "sb-")
        conj-str (fn [exps buf]
                   (if (seq buf)
                     (let [^String s (apply str buf)]
                       (conj exps `(.append ~sb ~s)))
                     exps))
        head-arg (fn [arg-seq]
                   (if (seq arg-seq)
                     (first arg-seq)
                     (throw (IllegalArgumentException. "Insufficient arguments to 'strfmt'"))))
        conj-exp (fn [exps buf new-exp]
                   (-> exps
                     (conj-str buf)
                     (conj new-exp)))]
    (loop [fmt  format-str ; remaining format string to process
           buf  []         ; buffer for the current contiguous string
           args args       ; remaining args to process
           exps []]        ; expressions to evaluate later
      (if (empty? fmt)
        `(with-obj-str [~sb (StringBuilder. ~(count format-str))]
           ~@(conj-str exps buf))
        (let [ch (first fmt)
              c2 (second fmt)]
          (if (= ch \%)
            (case c2
              \%  (recur (subs fmt 2) (conj buf c2) args exps)
              \B  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb (.toUpperCase
                                                                       (String/valueOf
                                                                         (boolean ~(head-arg args)))))))
              \b  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb (boolean ~(head-arg args)))))
              \d  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb (let [arg# ~(head-arg args)]
                                                                       (if (nil? arg#)
                                                                         "null"
                                                                         (long arg#))))))
              \f  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb (let [arg# ~(head-arg args)]
                                                                       (if (nil? arg#)
                                                                         "null"
                                                                         (double arg#))))))
              \H  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "NULL"
                                                                                 (.toUpperCase
                                                                                   (Integer/toHexString
                                                                                     (.hashCode ^Object arg#))))))))
              \h  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "null"
                                                                                 (Integer/toHexString
                                                                                   (.hashCode ^Object arg#)))))))
              \n  (recur (subs fmt 2) [] args (conj-exp exps buf
                                                `(.append ~sb i/line-separator)))
              \o  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "null"
                                                                                 (Long/toOctalString
                                                                                   (long arg#)))))))
              \S  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "NULL"
                                                                                 (.toUpperCase
                                                                                   (String/valueOf arg#)))))))
              \s  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "null"
                                                                                 (String/valueOf arg#))))))
              \X  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "NULL"
                                                                                 (.toUpperCase
                                                                                   (Long/toHexString
                                                                                     (long arg#))))))))
              \x  (recur (subs fmt 2) [] (next args) (conj-exp exps buf
                                                       `(.append ~sb ^String (let [arg# ~(head-arg args)]
                                                                               (if (nil? arg#)
                                                                                 "null"
                                                                                 (Long/toHexString
                                                                                   (long arg#)))))))
              nil (i/expected "either %b, %d, %f, %h, %n, %o, %s or %x" "%")
              (i/expected "either %b, %d, %f, %h, %n, %o, %s or %x" (str \% c2)))
            (recur (subs fmt 1) (conj buf ch) args exps)))))))


(defn repchar
  "Repeat a char given number of times and return the final string."
  [^long n ch]
  (if (pos? n)
    (.replace (String. (char-array n)) (char 0) (char ch))
    ""))


(defn repstr
  "Repeat a string given number of times and return the final string."
  [^long n token]
  (if (pos? n)
    (if (nil? token)
      ""
      (let [^String str-token (if (instance? String token) token (.toString ^Object token))]
        (with-obj-str [sb (StringBuilder. (unchecked-multiply n (.length str-token)))]
          (dotimes [_ n]
            (append! sb str-token)))))
    ""))


(defn strtbl
  "Return string representation of a textual table. Like clojure.pprint/print-table, but faster and does not print."
  ([ks rows]
    (let [^objects
          karray (object-array ks)
          kcount (alength karray)
          rowstr (mapv (fn [m]
                         (let [^objects cols (object-array kcount)]
                           (amap cols i r (strcat (get m (aget karray i))))))
                   rows)
          ^ints
          widths (int-array kcount)
          addrow (fn [^StringBuilder buffer ^String left-token ^String mid-token ^String right-token ^objects cols]
                   (append! buffer left-token)
                   (dotimes [i kcount]
                     (when (pos? i)
                       (append! buffer mid-token))
                     (let [^String colstr (aget cols i)]
                       (append! buffer (repchar (unchecked-subtract (aget widths i) (.length colstr)) \space))
                       (append! buffer colstr)))
                   (append! buffer right-token))]
      ;; set widths
      (dotimes [i kcount]
        (aset widths i
          ^int (apply max (.length ^String (strcat (aget karray i)))
                 (map #(.length ^String (aget ^objects % i)) rowstr))))
      ;; build string buffer
      (with-obj-str buffer
        ;; add header
        (addrow buffer "\n| " " | " " |" (amap karray i r (str (aget karray i))))
        (addrow buffer "\n|-" "-+-" "-|" (object-array (map #(repchar % \-) widths)))
        ;; add rows
        (doseq [^objects cols rowstr]
          (addrow buffer "\n| " " | " " |" cols)))))
  ([rows]
    (strtbl (keys (first rows)) rows)))
