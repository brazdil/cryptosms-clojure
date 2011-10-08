(ns uk.ac.cam.db538.cryptosms.low-level.data-types
  (:use clojure.test))

(defrecord ExportableType [export import length])
(defrecord ExportableItem [name type])

; NUMBERS

(with-test
  (defn- 
    ^{:doc "get-integer-byte [x i] returns the i-th byte in binary representation of unsigned integer x (max 64-bit)" }
    get-integer-byte [^Number x ^Number i]
      (if (or (> i 7) (< i 0))
        0
        (bit-and (bit-shift-right (long x) (* i 8)) 0xFF)))
  (is (= (get-integer-byte 0 0) 0))
  (is (= (get-integer-byte 1 0) 1))
  (is (= (get-integer-byte 1 1) 0))
  (is (= (get-integer-byte 255 0) 255))
  (is (= (get-integer-byte 255 1) 0))
  (is (= (get-integer-byte 256 0) 0))
  (is (= (get-integer-byte 256 1) 1))
  (is (= (get-integer-byte 65534 0) 254))
  (is (= (get-integer-byte 65534 1) 255))
  (is (= (get-integer-byte -1 0) 255))
  (is (= (get-integer-byte -2 0) 254))
  (is (not= (get-integer-byte 65536 6) 1))
  (is (= (get-integer-byte 2 -2) 0))
  (is (= (get-integer-byte 18446744073709551616 8) 0)))

(with-test
  (defn-
    ^{:doc "get-integer-bytes [x i] returns the i-byte binary representation of unsigned integer x" }
    get-integer-bytes [^Number x ^Number len]
    (loop [rem len
           accu []]
      (if (<= rem 0)
        accu
        (recur (dec rem) 
               (conj accu (get-integer-byte x (- rem 1)))))))
  (is (= (get-integer-bytes 0 -1) []))
  (is (= (get-integer-bytes 0 0) []))
  (is (= (get-integer-bytes 1 0) []))
  (is (= (get-integer-bytes 0 1) [0]))
  (is (= (get-integer-bytes 1 1) [1]))
  (is (= (get-integer-bytes 255 1) [255]))
  (is (= (get-integer-bytes 256 1) [0]))
  (is (= (get-integer-bytes -1 1) [255]))
  (is (= (get-integer-bytes -2 1) [254]))
  (is (= (get-integer-bytes 0 2) [0 0]))
  (is (= (get-integer-bytes 1 2) [0 1]))
  (is (= (get-integer-bytes 255 2) [0 255]))
  (is (= (get-integer-bytes 256 2) [1 0]))
  (is (= (get-integer-bytes 65535 2) [255 255]))
  (is (= (get-integer-bytes 65536 2) [0 0]))
  (is (= (get-integer-bytes -1 2) [255 255]))
  (is (= (get-integer-bytes -2 2) [255 254])))

(defn- pow [base exp]
  (letfn 
    [(kapow [base exp acc]
      (if (zero? exp)
        acc
        (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))

(with-test
  ^{:doc "parse-integer-bytes [xs] returns an integer which is represented by a given byte-array" }
  (defn- parse-integer-bytes [xs]
    (loop [xs xs, accu 0, exp 0]
      (if (empty? xs)
        accu
        (if (or (< (peek xs) 0) (> (peek xs) 255))
          (throw (new IllegalArgumentException))
          (recur (pop xs) (+ accu (* (peek xs) (pow 2 exp))) (+ exp 8))))))
  (is (= (parse-integer-bytes []) 0))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [-1])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [256])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 2 -1])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 -1 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [-1 1 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 2 256])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [1 256 2])))
  (is (thrown? IllegalArgumentException (parse-integer-bytes [256 1 2])))
  (is (= (parse-integer-bytes [1]) 1))
  (is (= (parse-integer-bytes [255]) 255))
  (is (= (parse-integer-bytes [1 1]) 257))
  (is (= (parse-integer-bytes [1 1 1]) 65793))
  (is (= (parse-integer-bytes [2 1 1 1]) 33620225)))

(with-test
  (defn- 
    ^{:doc "uint-type-factory [len] returns an exportable type for unsigned integer with given byte length" }
    uint-type-factory [name ^Number len]
    (if (or (< len 1) (> len 8))
      (throw (new IllegalArgumentException))
      (ExportableType.
        ; export
        (fn [data] (get-integer-bytes (name data) len))
        ; import 
        (fn [^bytes xs]
          (if (not= (count xs) len)
            (throw (new IllegalArgumentException))
            {name (parse-integer-bytes xs)}))
        ; length
        (fn [] len) )))
  (is (thrown? IllegalArgumentException (uint-type-factory :test -1)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 0)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 9)))
  (is (thrown? IllegalArgumentException (uint-type-factory :test 10)))
  (is (= ((:export (uint-type-factory :test 3)) {:test 0x123456}) [ 0x12 0x34 0x56 ]))
  (is (= ((:import (uint-type-factory :test 3)) [ 0xAB 0xCD 0xEF ]) {:test 0xABCDEF}))
  (is (= ((:length (uint-type-factory :test 3))) 3)))

(defn uint8 [name] (uint-type-factory name 1))
(defn uint16 [name] (uint-type-factory name 2))
(defn uint32 [name] (uint-type-factory name 4))
(defn uint64 [name] (uint-type-factory name 8))

; COMPOSITE

(defn composite [exportables]
  (ExportableType.
    ; export
    (fn [data] (reduce #(reduce conj %1 %2) [] (map #((:export %) data) exportables)))
    ; import
    nil
    ; length
    nil))
