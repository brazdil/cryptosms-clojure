(ns uk.ac.cam.db538.cryptosms.utils
  (:use [clojure.test :only (with-test, is) ]))
  
; debugging parts of expressions
(defmacro dbg [x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

; byte-array Java type
(defn type-byte-array [] (java.lang.Class/forName "[B"))

; round-up division
(with-test
  (defn round-up-div [^Number number ^Number divisor]
    (quot (+ number (- divisor 1)) divisor))
  (is (= (round-up-div 5 2) 3))
  (is (= (round-up-div 25 16) 2))
  (is (= (round-up-div 15 5) 3)) )

; least greater multiple
(with-test
  (defn least-greater-multiple [^Number number ^Number multiple]
    (* (round-up-div number multiple) multiple))
  (is (= (least-greater-multiple 7 2) 8))
  (is (= (least-greater-multiple 25 16) 32))
  (is (= (least-greater-multiple 15 5) 15)) )
