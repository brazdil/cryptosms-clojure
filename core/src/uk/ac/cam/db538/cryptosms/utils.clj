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

(with-test
  (defn HEX [ hex ]
    (if (string? hex)
      ; hex string to vector of bytes
	    (if (odd? (count hex))
	      (throw (new IllegalArgumentException))
		    (loop [ accu []
		            hex hex ]
		      (if (empty? hex)
		        accu
		        (recur (conj accu (Integer/parseInt (str (get hex 0) (get hex 1)) 16)) (subs hex 2)))))
      (if (vector? hex)
        ; vector of bytes to string
        (reduce str ""
          (map #(if (= (count %) 1) (str "0" %) %)  
	          (map #(if (or (< % 0) (> % 255))
	                   (throw (new IllegalArgumentException))
	                   (Integer/toHexString %)) hex)))
        ; not recognized
        (throw (new IllegalArgumentException)))))
  ; string -> vector
  (is (thrown? IllegalArgumentException (HEX "x")))
  (is (thrown? IllegalArgumentException (HEX "1x1")))
  (is (thrown? NumberFormatException (HEX "0123x56789ABCDEF")))
  (is (thrown? NumberFormatException (HEX "012x456789ABCDEF")))
  (is (= (HEX "") []))
  (is (= (HEX "ff") [255]))
  (is (= (HEX "FF") [255]))
  (is (= (HEX "fF") [255]))
  (is (= (HEX "1234") [0x12 0x34]))
  ; vector -> string
  (is (thrown? IllegalArgumentException (HEX [ 1 2 3 -1] )))
  (is (thrown? IllegalArgumentException (HEX [ 1 256 2 3] )))
  (is (= (HEX []) ""))
  (is (= (HEX [255] ) "ff"))
  (is (= (HEX [0x12 0x34]) "1234")))
      