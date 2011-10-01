(ns uk.ac.cam.db538.cryptosms.low-level.data-types
  (:use clojure.test))

(with-test
  (defn 
    ^{:doc "get-byte [x i] returns the i-th byte in binary representation of unsigned integer x (max 64-bit)" }
    get-byte [^Number x ^Number i]
      (if (or (> i 7) (< i 0)))
        0
        (bit-and (bit-shift-right (long x) (* i 8)) 0xFF))
  (is (= (get-byte 0 0) 0))
  (is (= (get-byte 1 0) 1))
  (is (= (get-byte 1 1) 0))
  (is (= (get-byte 255 0) 255))
  (is (= (get-byte 255 1) 0))
  (is (= (get-byte 256 0) 0))
  (is (= (get-byte 256 1) 1))
  (is (= (get-byte 65534 0) 254))
  (is (= (get-byte 65534 1) 255))
  (is (= (get-byte -1 0) 255))
  (is (= (get-byte -2 0) 254))
  (is (not= (get-byte 65536 6) 1))
  (is (= (get-byte 2 -2) 0))
  (is (= (get-byte 18446744073709551616 8) 0)))

(with-test
  (defn
    ^{:doc "get-bytes [x i] returns the i-byte binary representation of unsigned integer x" }
    get-bytes [^Number x ^Number len]
    (loop [rem len
           accu []]
      (if (<= rem 0)
        accu
        (recur (dec rem) 
               (conj accu (get-byte x (- rem 1)))))))
  (is (= (get-bytes 0 -1) []))
  (is (= (get-bytes 0 0) []))
  (is (= (get-bytes 1 0) []))
  (is (= (get-bytes 0 1) [0]))
  (is (= (get-bytes 1 1) [1]))
  (is (= (get-bytes 255 1) [255]))
  (is (= (get-bytes 256 1) [0]))
  (is (= (get-bytes -1 1) [255]))
  (is (= (get-bytes -2 1) [254]))
  (is (= (get-bytes 0 2) [0 0]))
  (is (= (get-bytes 1 2) [0 1]))
  (is (= (get-bytes 255 2) [0 255]))
  (is (= (get-bytes 256 2) [1 0]))
  (is (= (get-bytes 65535 2) [255 255]))
  (is (= (get-bytes 65536 2) [0 0]))
  (is (= (get-bytes -1 2) [255 255]))
  (is (= (get-bytes -2 2) [255 254])))

(defn 
  ^{:doc "uint8 [x] returns the 1-byte binary representation of unsigned integer x" }
  uint16 [^Number x] (get-bytes x 1))

(defn 
  ^{:doc "uint16 [x] returns the 2-byte binary representation of unsigned integer x" }
  uint16 [^Number x] (get-bytes x 2))

(defn 
  ^{:doc "uint32 [x] returns the 4-byte binary representation of unsigned integer x" }
  uint32 [^Number x] (get-bytes x 4))

(defn 
  ^{:doc "uint64 [x] returns the 8-byte binary representation of unsigned integer x" }
  uint64 [^Number x] (get-bytes x 8))
  