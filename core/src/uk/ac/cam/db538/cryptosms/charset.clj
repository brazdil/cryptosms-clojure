(ns uk.ac.cam.db538.cryptosms.charset
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] ))

(defn ASCII8 
  "Given a string, returns byte-vector with 8-bit ASCII representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "US-ASCII"))
    (if (vector? data)
      (new String (byte-arrays/output data) "US-ASCII")
      (throw (new IllegalArgumentException)))))

(defn UTF8 
  "Given a string, returns byte-vector with UTF-8 representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "UTF-8"))
    (if (vector? data)
      (new String (byte-arrays/output data) "UTF-8")
      (throw (new IllegalArgumentException)))))

(defn UTF16 
  "Given a string, returns byte-vector with UTF-16 representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "UTF-16"))
    (if (vector? data)
      (new String (byte-arrays/output data) "UTF-16")
      (throw (new IllegalArgumentException)))))

(with-test
  (defn length-in-ascii7 
    "Returns length of string if represented as 7-bit ASCII. Argument is number of characters (length of the string)."
    [ ^Number len ]
    (utils/round-up-div (* len 7) 8))
  (is (= (length-in-ascii7 0) 0))
  (is (= (length-in-ascii7 1) 1))
  (is (= (length-in-ascii7 7) 7))
  (is (= (length-in-ascii7 8) 7))
  (is (= (length-in-ascii7 14) 13))
  (is (= (length-in-ascii7 15) 14))
  (is (= (length-in-ascii7 16) 14))
  (is (= (length-in-ascii7 30) 27))
  (is (= (length-in-ascii7 31) 28))
  (is (= (length-in-ascii7 32) 28)))

(with-test
  (defn ASCII7 
    "Given a string, returns byte-vector with 7-bit ASCII representation of that string.
     Given a vector, does the reverse."
    [ data ]
    (if (string? data)
      (letfn [ (separate [ ^bytes xs ]
                 (loop [ xs xs
                         separation-point 0
                         accu (transient []) ]
                   (if (empty? xs)
                     (if (= separation-point 0)
                       (persistent! accu)
                       (persistent! (conj! accu 0)))
                     (recur 
                       (subvec xs 1) 
                       (mod (+ separation-point 1) 8)
                       (let [ byte1 (bit-and (bit-shift-right (xs 0) (- 7 separation-point)) 0xFF)
                              byte2 (bit-and (bit-shift-left (xs 0) (+ 1 separation-point)) 0xFF) ]
                         (if (= separation-point 0)
                           (conj! accu byte2)
                           (if (= separation-point 7)
                           (conj! accu byte1)
                           (conj! (conj! accu byte1) byte2)))))))) ]
        (loop [ separated (separate (ASCII8 data))
                accu (transient []) ]
          (if (empty? separated)
            (persistent! accu)
            (recur
              (subvec separated 2)
              (conj! accu (bit-or (separated 0) (separated 1)))))))
      (if (vector? data)
        (ASCII8 data))))
  (is (= (ASCII7 "") []))
  (is (= (ASCII7 "1") [ 0x62 ]))
  (is (= (ASCII7 "11") [ 0x62 0xC4 ]))
  (is (= (ASCII7 "111") [ 0x62 0xC5 0x88 ]))
  (is (= (ASCII7 "1111") [ 0x62 0xC5 0x8b 0x10 ]))
  (is (= (ASCII7 "11111") [ 0x62 0xC5 0x8b 0x16 0x20 ]))
  (is (= (ASCII7 "111111") [ 0x62 0xC5 0x8b 0x16 0x2c 0x40 ]))
  (is (= (ASCII7 "1111111") [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0x80 ] ))
  (is (= (ASCII7 "11111111") [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0xb1 ] ))
  (is (= (ASCII7 "111111111") [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0xb1 0x62 ] ))
  (is (= (ASCII7 "Hello, world!!!") (utils/HEX "919766cdeb1077dfcb6644285080"))) )
