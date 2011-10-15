(ns uk.ac.cam.db538.cryptosms.low-level.serialize
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.crypto.aes :as aes]
            [uk.ac.cam.db538.cryptosms.utils :as utils]))

(defrecord Serializable [ export import length ])

; UNSIGNED INTEGERS

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
      (Serializable.
        ; export
        (fn [data] (get-integer-bytes (name data) len))
        ; import 
        (fn [^bytes xs]
          (if (not= (count xs) len)
            (throw (new IllegalArgumentException))
            {name (parse-integer-bytes xs)}))
        ; length
        len )))
  (is (thrown? IllegalArgumentException (uint-type-factory :test -1)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 0)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 9)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 10)))
  (is (= ((:export (uint-type-factory :test 3)) {:test 0x123456}) [ 0x12 0x34 0x56 ]))
  (is (= ((:import (uint-type-factory :test 3)) [ 0xAB 0xCD 0xEF ]) {:test 0xABCDEF}))
  (is (= (:length (uint-type-factory :test 3)) 3)))

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

; COMPOSITE

(with-test
  (defn composite 
    "Returns a serializable type combining together list of other serializables."
    [exportables]
    (let [ composite-length (reduce + 0 (map #(:length %) exportables)) ]
      (Serializable.
        ; export
        (fn [data] (persistent! (reduce #(reduce conj! %1 %2) (transient []) (map #((:export %) data) exportables))))
        ; import
        (fn [^bytes xs] 
          (if (not= (count xs) composite-length)
            (throw (new IllegalArgumentException))
            (let [ offsets (vec (reductions + 0 (map #(:length %) exportables))) ; offsets of items (e.g [ 0 2 6 ] for uin16 and uint32 - last is ignored!!!)
                   ends (subvec offsets 1) ; offsets of following items, e.g [ 2 6 ] as in previous line
                   subvecs (map #(subvec xs %1 %2) offsets ends) ] ; subvectors passed to individual items
              (persistent! (reduce conj! (transient {}) (map #((:import %1) %2) exportables subvecs))))))
        ; length
        composite-length )))
    (let [ items [ (uint8 :item1) (uint16 :item2) (uint32 :item3) (uint64 :item4) ]
           data { :item1 0x12, :item2 0x1234, :item3 0x12345678, :item4 0x1234567890ABCDEF} 
           result [ 0x12 0x12 0x34 0x12 0x34 0x56 0x78 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF ]
         ]
      (is (thrown? IllegalArgumentException ((:import (composite items)) (vec (range 0 (+ (count result) 1))) )))
      (is (thrown? IllegalArgumentException ((:import (composite items)) (vec (range 0 (- (count result) 1))) )))
      (is (= ((:export (composite [])) {}) []))
      (is (= ((:import (composite [])) []) {}))
      (is (= (:length (composite [])) 0))
      (is (= ((:export (composite items)) data) result))
      (is (= ((:import (composite items)) result) data))
      (is (= (:length (composite items)) (count result))) ))

; ALIGN

(with-test
  (defn align
    "Returns a serializable type which aligns given serializable to given length."
    [^Number length-aligned exportable]
    (let [ length-random (- length-aligned (:length exportable)) ]
      (if (< length-random 0) ; handles negative alignment length as well
        (throw (new IllegalArgumentException))
        (Serializable.
          ; export
          (fn [data] (persistent! (reduce conj! (transient ((:export exportable) data)) (random/rand-next length-random) )))
          ; import
          (fn [^bytes xs]
            (if (not= (count xs) length-aligned)
              (throw (new IllegalArgumentException))
              ((:import exportable) (subvec xs 0 (:length exportable)))))
          ; length
          length-aligned ))))
  (let [ item (uint64 :id)
         data { :id 0x1234567890ABCDEF }
         result [ 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF ]
         result-aligned [ 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF 0xFE 0xDC 0xBA 0x09 0x87 0x65 0x43 0x21 ] ]
    (is (thrown? IllegalArgumentException (align -1 item)))
    (is (thrown? IllegalArgumentException (align 7 item)))
    (is (= (count ((:export (align 128 item)) data)) 128))
    (is (= (subvec ((:export (align 128 item)) data) 0 8 ) result))
    (is (= ((:import (align 16 item)) result-aligned) data))
    (is (= (:length (align 128 item)) 128)) ))

; SAMPLE

(def structure
  (align 32
    (composite [
      (uint16 :ushort1)
      (uint16 :ushort2)
      (uint32 :uint1)
      (uint64 :ulong1) ])))

(def data { :ushort1 0xFFFF, :ushort2 0xAAAA, :uint1 0x10203040, :ulong1 0x0102030405060708 } )
