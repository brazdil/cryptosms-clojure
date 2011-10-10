(ns uk.ac.cam.db538.cryptosms.low-level.byte-arrays
  (:use [clojure.test :only (with-test, is) ]))

; CONVERT TO BYTE ARRAY

(with-test
  (defn output [vector]
    (byte-array 
      (map #(byte 
				(if (or (< % 0) (> % 255))
				  (throw (new IllegalArgumentException))
				  (if (> % 127) (- % 256) %))) vector)))
	(is (thrown? IllegalArgumentException (output [ -1 ])))
	(is (thrown? IllegalArgumentException (output [ 256 ])))
	(is (= (vec (output [])) []))
	(is (= (vec (output [ 0 1 2 3 ])) [ 0 1 2 3 ] ))
	(is (= (vec (output [ 255 254 253 ])) [ -1 -2 -3 ])) )

(with-test
  (defn input [^bytes array]
	  (vec (map #(if (< % 0) (+ % 256) %) (vec array))))
  (is (= (input (byte-array (map #(byte %) [ 0 1 2 3 ]))) [ 0 1 2 3 ]))
  (is (= (input (byte-array (map #(byte %) [ -1 -2 -3 ]))) [ 255 254 253 ])) ) 
