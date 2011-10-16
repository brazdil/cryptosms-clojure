(ns uk.ac.cam.db538.cryptosms.serializables.uint
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common]))

(with-test
  (defn- get-integer-byte
    "get-integer-byte [x i] returns the i-th byte in binary representation of unsigned integer x (max 63-bit)"
    [^Number x ^Number i]
    (if (or (< x 0) (> x 0x7FFFFFFFFFFFFFFF) (> i 7) (< i 0))
      (throw (new IllegalArgumentException))
      (bit-and (bit-shift-right (long x) (* i 8)) 0xFF)))
  (is (thrown? IllegalArgumentException (get-integer-byte -1 0)))
  (is (thrown? IllegalArgumentException (get-integer-byte 2 -1)))
  (is (thrown? IllegalArgumentException (get-integer-byte 2 8)))
  (is (= (get-integer-byte 0 0) 0))
  (is (= (get-integer-byte 1 0) 1))
  (is (= (get-integer-byte 1 1) 0))
  (is (= (get-integer-byte 255 0) 255))
  (is (= (get-integer-byte 255 1) 0))
  (is (= (get-integer-byte 256 0) 0))
  (is (= (get-integer-byte 256 1) 1))
  (is (= (get-integer-byte 65534 0) 254))
  (is (= (get-integer-byte 65534 1) 255))
  (is (not= (get-integer-byte 65536 6) 1)))

(with-test
  (defn- get-integer-bytes
    "get-integer-bytes [x i] returns the i-byte binary representation of unsigned integer x"
    [^Number x ^Number len]
    (if (< len 0)
      (throw (new IllegalArgumentException))
      (loop [rem len
             accu []]
        (if (<= rem 0)
          accu
          (recur (dec rem) 
                 (conj accu (get-integer-byte x (- rem 1))))))))
  (is (thrown? IllegalArgumentException (get-integer-bytes -1 1)))
  (is (thrown? IllegalArgumentException (get-integer-bytes 0 -1)))
  (is (thrown? IllegalArgumentException (get-integer-bytes 0x8FFFFFFFFFFFFFFF 8)))
  (is (= (get-integer-bytes 0 0) []))
  (is (= (get-integer-bytes 1 0) []))
  (is (= (get-integer-bytes 0 1) [0]))
  (is (= (get-integer-bytes 1 1) [1]))
  (is (= (get-integer-bytes 255 1) [255]))
  (is (= (get-integer-bytes 256 1) [0]))
  (is (= (get-integer-bytes 0 2) [0 0]))
  (is (= (get-integer-bytes 1 2) [0 1]))
  (is (= (get-integer-bytes 255 2) [0 255]))
  (is (= (get-integer-bytes 256 2) [1 0]))
  (is (= (get-integer-bytes 65535 2) [255 255]))
  (is (= (get-integer-bytes 65536 2) [0 0]))
  (is (= (get-integer-bytes 0x7FFFFFFFFFFFFFFF 8) [127 255 255 255 255 255 255 255])))

(with-test
  (defn- parse-integer-bytes 
    "parse-integer-bytes [xs] returns an integer which is represented by a given byte-array"
    [xs]
    (if (or (> (count xs) 8) (and (= (count xs) 8) (> (xs 0) 127)) )
      (throw (new IllegalArgumentException))
      (loop [xs xs, accu (long 0)]
        (if (empty? xs)
          accu
          (if (or (< (xs 0) 0) (> (xs 0) 255))
            (throw (new IllegalArgumentException))
            (recur (subvec xs 1) (bit-or (bit-shift-left accu 8) (xs 0))))))))
  (is (= (parse-integer-bytes []) 0))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [-1])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [256])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 2 -1])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 -1 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [-1 1 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 2 256])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 256 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [256 1 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [128 1 2 3 4 5 6 7])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 2 3 4 5 6 7 8 9])))
  (is (= (parse-integer-bytes [1]) 1))
  (is (= (parse-integer-bytes [255]) 255))
  (is (= (parse-integer-bytes [1 1]) 257))
  (is (= (parse-integer-bytes [1 1 1]) 65793))
  (is (= (parse-integer-bytes [2 1 1 1]) 33620225))
  (is (= (parse-integer-bytes [0x7F 0x01 0x02 0x03 0x04 0x05 0x06 0x07]) 0x7f01020304050607)))

(with-test
  (defn- uint-type-factory
    "uint-type-factory [len] returns an exportable type for unsigned integer with given byte length"
    [name ^Number len]
    (if (or (< len 1) (> len 8))
      (throw (new IllegalArgumentException))
      (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
        ; export
        (fn [data] (get-integer-bytes (name data) len))
        ; import 
        (fn [^bytes xs args]
          (if (< (count xs) len)
            (throw (new IllegalArgumentException))
            {name (parse-integer-bytes (subvec xs 0 len))}))
        ; length
        (fn [data] len) )))
  (is (thrown? IllegalArgumentException (uint-type-factory :test -1)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 0)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 9)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 10)))
  (is (= ((:export (uint-type-factory :test 3)) {:test 0x123456}) [ 0x12 0x34 0x56 ]))
  (is (= ((:import (uint-type-factory :test 3)) [ 0xAB 0xCD 0xEF ] {}) {:test 0xABCDEF}))
  (is (= ((:length (uint-type-factory :test 3)) {} ) 3)))

(defn uint8 
  "Returns a serializable type for 8-bit unsigned integer"
  [name] (uint-type-factory name 1))
  
(defn uint16
  "Returns a serializable type for 16-bit unsigned integer"
  [name] (uint-type-factory name 2))

(defn uint32
  "Returns a serializable type for 32-bit unsigned integer"
  [name] (uint-type-factory name 4))

(defn uint64
  "Returns a serializable type for 63-bit (!!!) unsigned integer"
  [name] (uint-type-factory name 8))

