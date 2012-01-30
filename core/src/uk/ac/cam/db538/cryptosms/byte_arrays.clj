(ns uk.ac.cam.db538.cryptosms.byte-arrays
  (:use [clojure.test :only (with-test, is) ]))

; CONVERT TO BYTE ARRAY

(defn #^"[B" create 
  "Creates a Java byte array of given length."
  [^Number len]
  (byte-array (repeat len (byte 0))))

(with-test
  (defn from-vector
    "Turns a Clojure vector into Java byte array. Expects the Clojure vector to contain unsigned bytes (numbers between 0-255)."
    [#^clojure.core.Vec vector]
    (byte-array 
      (map #(byte 
				(if (or (< % 0) (> % 255))
				  (throw (new IllegalArgumentException))
				  (if (> % 127) (- % 256) %))) vector)))
	(is (thrown? IllegalArgumentException (from-vector [ -1 ])))
	(is (thrown? IllegalArgumentException (from-vector [ 256 ])))
	(is (= (vec (from-vector [])) []))
	(is (= (vec (from-vector [ 0 1 2 3 ])) [ 0 1 2 3 ] ))
	(is (= (vec (from-vector [ 255 254 253 ])) [ -1 -2 -3 ])) )

(with-test
  (defn #^clojure.core.Vec  to-vector
    "Turns a Java byte array into a Clojure vector. Produces vector with unsigned integers (numbers between 0-255)."
    [#^"[B" array]
    (loop [ pos 0
            accu (into (vector-of :int) array) ]
      (if (>= pos (count accu))
        accu
        (let [ head (accu pos) ]
         (recur 
           (inc pos) 
           (if (< head 0)
             (assoc accu pos (+ head 256))
             accu))))))
  (is (= (to-vector (byte-array (map #(byte %) [ 0 1 2 3 ]))) [ 0 1 2 3 ]))
  (is (= (to-vector (byte-array (map #(byte %) [ -1 -2 -3 ]))) [ 255 254 253 ])) ) 
  
; byte-array Java type
(defn java-type [] (java.lang.Class/forName "[B"))

