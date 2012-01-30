(ns uk.ac.cam.db538.cryptosms.charset
  (:use [clojure.test :only (with-test, is, deftest) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] ))

(defmulti ASCII8 
  "Given a string, returns byte-vector with 8-bit ASCII representation of that string.
   Given a vector, does the reverse."
  utils/string-vector)

(defmethod ASCII8 :string [ #^String data ]
  (byte-arrays/to-vector (. data getBytes "US-ASCII")))
  
(defmethod ASCII8 :vector [ #^clojure.core.Vec data ]
  (new String (byte-arrays/from-vector data) "US-ASCII"))

(defmulti UTF8 
  "Given a string, returns byte-vector with UTF-8 representation of that string.
   Given a vector, does the reverse."
  utils/string-vector)
  
(defmethod UTF8 :string [ #^String data ]
  (byte-arrays/to-vector (. data getBytes "UTF-8")))
  
(defmethod UTF8 :vector [ #^clojure.core.Vec data ]
  (new String (byte-arrays/from-vector data) "UTF-8"))

(defmulti UTF16 
  "Given a string, returns byte-vector with UTF-16 representation of that string.
   Given a vector, does the reverse."
  utils/string-vector)
  
(defmethod UTF16 :string [ #^String data ]
  (byte-arrays/to-vector (. data getBytes "UTF-16")))
  
(defmethod UTF16 :vector [ #^clojure.core.Vec data ]
  (new String (byte-arrays/from-vector data) "UTF-16"))

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

(defn- byte-separate-in
  "Takes a byte-vector and prepares it to be turned into 7-bit compressed format.
   The algorithm divides each byte into two parts at different points. Result
   is a byte-vector, where two adjacent bytes contain bits from the two adjecent 
   bytes of original vector and these need to be or-ed together to produce a byte
   in the resulting 7-bit representation."
 [ ^bytes xs ]
  (loop [ xs xs
         separation-point 0
         accu (vector-of :int) ]
    (if (empty? xs)
      (if (= separation-point 0)
        accu
        (conj accu 0))
      (recur 
        (subvec xs 1) 
        (mod (+ separation-point 1) 8)
        (let [ byte1 (bit-and (bit-shift-right (xs 0) (- 7 separation-point)) 0xFF)
               byte2 (bit-and (bit-shift-left (xs 0) (+ 1 separation-point)) 0xFF) ]
          (if (= separation-point 0)
            (conj accu byte2)
            (if (= separation-point 7)
              (conj accu byte1)
              (conj (conj accu byte1) byte2))))))))

(defn- byte-separate-out [ ^bytes xs ]
  "Expects a 7-bit representation of data and prepares it to be turned back into 
  an 8-bit representation by splitting bytes after each 7 bits. Resulting byte-vector
  is twice the size as original since two adjecent bytes have to be or-ed together
  to form the 8-bit representation."  
  (loop [ xs xs
         separation-point 6
         accu (vector-of :int) ]
    (if (empty? xs)
      (if (= separation-point 6)
        accu
        (conj accu 0))
      (recur
        (subvec xs 1)
        (mod (- separation-point 1) 7)
        (let [ byte1 (bit-and (bit-shift-right (xs 0) (- 7 separation-point)) 0xFF)
               byte2 (bit-and (bit-shift-left (xs 0) separation-point) 0x7F) ]
          (if (= separation-point 6)
            (conj (conj (conj accu 0x00) byte1) byte2)
            (if (= separation-point 0)
              (conj (conj (conj accu byte1) byte2) 0x00)
              (conj (conj accu byte1) byte2))))))))

(defmulti ASCII7 
  "Given a string, returns byte-vector with 7-bit ASCII representation of that string.
   Given a vector, does the reverse."
  utils/string-vector)
  
(defmethod ASCII7 :string [ #^String data ]
  (loop [ separated (byte-separate-in (ASCII8 data))
          accu (vector-of :int) ]
    (if (empty? separated)
      accu
      (recur
        (subvec separated 2)
        (conj accu (bit-or (separated 0) (separated 1)))))))
        
(defmethod ASCII7 :vector [ #^clojure.core.Vec data ]
  (loop [ separated (byte-separate-out data)
          accu (vector-of :int) ]
    (if (empty? separated)
      (if (and (> (count accu) 0) (= (accu (- (count accu) 1)) 0))
        (ASCII8 (subvec accu 0 (- (count accu) 1))) ; gets rid of redundant zero at the end
        (ASCII8 accu))
      (recur
        (subvec separated 2)
        (conj accu (bit-or (separated 0) (separated 1)))))))

(deftest testASCII7
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
  (is (= (ASCII7 "Hello, world!!!") (utils/HEX "919766cdeb1077dfcb6644285080")))
  (is (= "" (ASCII7 [])))
  (is (= "1" (ASCII7 [ 0x62 ])))
  (is (= "11" (ASCII7 [ 0x62 0xC4 ])))
  (is (= "111" (ASCII7 [ 0x62 0xC5 0x88 ])))
  (is (= "1111" (ASCII7 [ 0x62 0xC5 0x8b 0x10 ])))
  (is (= "11111" (ASCII7 [ 0x62 0xC5 0x8b 0x16 0x20 ])))
  (is (= "111111" (ASCII7 [ 0x62 0xC5 0x8b 0x16 0x2c 0x40 ])))
  (is (= "1111111" (ASCII7 [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0x80 ] )))
  (is (= "11111111" (ASCII7 [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0xb1 ] )))
  (is (= "111111111" (ASCII7 [ 0x62 0xC5 0x8b 0x16 0x2c 0x58 0xb1 0x62 ] )))
  (is (= "Hello, world!!!" (ASCII7 (utils/HEX "919766cdeb1077dfcb6644285080")))) )
