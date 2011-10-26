(ns uk.ac.cam.db538.cryptosms.string-compression
  (:use [clojure.test :only (with-test, is, deftest) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.charset :as charset]
            [uk.ac.cam.db538.cryptosms.zlib :as zlib]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random] ))

(defn- compressed-string-type 
  "Three most significant bits of header represent the encoding and compression"
  [^bytes data]
  (let [ header (data 0) ]
    (case (bit-and header 2r11100000)
      2r00000000 :ascii7
      2r01000000 :ascii8
      2r01100000 :ascii8-zlib
      2r10000000 :utf8
      2r10100000 :utf8-zlib
      2r11000000 :utf16
      2r11100000 :utf16-zlib
                 :unknown )))

(defn- compressed-string-alignment
  "Fourth bit of header represents a boolean value, whether string is aligned to 16 bytes."
  [^bytes data]
  (not= (bit-and 2r00010000 (data 0)) 0))

(defn- compressed-string-garbage 
  "Four least significant bits represent the length of garbage added for alignment."
  [^bytes data]
  (bit-and 2r00001111 (data 0)))

(defn compress 
  "Algorithm automatically tries representing the text as ASCII7, ASCII8, UTF-8 and UTF-16
   and compressing each with DEFLATE algorithm. Then chooses the shortest representation.
   ASCII encodings are tried only if the string doesn't contain any non-ASCII characters.
   Returns a byte-vector."
  [^String data]
  (letfn [ (contains-only-ascii [data]
             (loop [ pos 0 ]
               (if (>= pos (count data))
                 true
                 (let [ first-char (int (.charAt data 0)) ]
                   (if (or (< first-char 0) (> first-char 127))
                     false
                     (recur (+ pos 1))))))) ]
    (if (contains-only-ascii data)
      ; ASCII
      (let [ data-ascii8          (charset/ASCII8 data)
             data-ascii8-zlib     (zlib/deflate data-ascii8)
             length-ascii7        (charset/length-in-ascii7 (count data))
             length-ascii8        (count data-ascii8)
             length-ascii8-zlib   (count data-ascii8-zlib) ]
        (if (and (<= length-ascii8 length-ascii7) (<= length-ascii8 length-ascii8-zlib))
          ; best compression is ASCII8 (preferable)
          (reduce conj (conj (vector-of :int) 2r01000000) data-ascii8) ; ASCII8 = 01, no compression = 0
          (if (<= length-ascii7 length-ascii8-zlib)
            ; best compression is ASCII7
            (reduce conj (conj (vector-of :int) 2r00000000) (charset/ASCII7 data)) ; ASCII7 = 00, no compression = 0
            ; best compression is ASCII8-DEFLATE
            (reduce conj (conj (vector-of :int) 2r01100000) data-ascii8-zlib)))) ; ASCII8 = 01, compression = 1
      ; UNICODE
      (let [ data-utf8            (charset/UTF8 data)
             data-utf8-zlib       (zlib/deflate data-utf8)
             length-utf8          (count data-utf8)
             length-utf8-zlib     (count data-utf8-zlib)
             data-utf16           (charset/UTF16 data)
             data-utf16-zlib      (zlib/deflate data-utf16)
             length-utf16         (count data-utf16)
             length-utf16-zlib    (count data-utf16-zlib) ]
        (if (and (< length-utf8-zlib length-utf8) (< length-utf8-zlib length-utf16) (< length-utf8-zlib length-utf16-zlib))
          ; best is UTF8-DEFLATE (usually the best)
          (reduce conj (conj (vector-of :int) 2r10100000) data-utf8-zlib) ; UTF8 = 10, compression = 1
          (if (and (< length-utf16-zlib length-utf16) (< length-utf16-zlib length-utf8))
            ; best is UTF16-DEFLATE
            (reduce conj (conj (vector-of :int) 2r11100000) data-utf16-zlib) ; UTF16 = 11, compression = 1
            (if (< length-utf8 length-utf16)
              ; best is UTF8
              (reduce conj (conj (vector-of :int) 2r10000000) data-utf8) ; UTF8 = 10, no compression = 0
              ; best is UTF16
              (reduce conj (conj (vector-of :int) 2r11000000) data-utf16)))))))) ; UTF16 = 11, no compression = 0

(defn decompress
  "Decompresses a previously compressed string. Expects a byte-vector and returns a string."
  [^bytes data]
  (let [ data-headerless   (subvec data 1) ]
    (case (compressed-string-type data)
      :ascii7        (charset/ASCII7 data-headerless)
      :ascii8        (charset/ASCII8 data-headerless)
      :ascii8-zlib   (charset/ASCII8 (zlib/inflate data-headerless))
      :utf8          (charset/UTF8 data-headerless)
      :utf8-zlib     (charset/UTF8 (zlib/inflate data-headerless))
      :utf16         (charset/UTF16 data-headerless)
      :utf16-zlib    (charset/UTF16 (zlib/inflate data-headerless)))))

(defn compress-align
  "Algorithm automatically tries representing the text as ASCII7, ASCII8, UTF-8 and UTF-16
   and compressing each with DEFLATE algorithm. Then chooses the shortest representation.
   ASCII encodings are tried only if the string doesn't contain any non-ASCII characters.
   Returns a byte-vector aligned to 16 bytes (AES block size)."
  [^String data]
  (let [ compressed        (compress data)
         length-garbage    (mod (- 16 (count compressed)) 16)
         new-header        (bit-or (compressed 0) (bit-or 2r00010000 length-garbage)) ]
    (reduce conj (assoc compressed 0 new-header) (random/next-vector length-garbage))))

(defn decompress-aligned
  "Decompresses a previously compressed string. Expects a byte-vector and returns a string."
  [^bytes data]
  (if (compressed-string-alignment data)
    (let [ no-garbage      (subvec data 0 (- (count data) (compressed-string-garbage data)))
           old-header      (bit-and (data 0) 2r11100000) ]
      (decompress (assoc no-garbage 0 old-header)))
    (decompress data)))

(with-test
  (defn- compressed-string-test [data charset]
    (let [ compressed (compress data)
           decompressed (decompress compressed)
           compressed-aligned (compress-align data)
           decompressed-aligned (decompress-aligned compressed-aligned) ]
      (is (= data decompressed))
      (is (= data decompressed-aligned))
      (is (= (subvec compressed-aligned 1 (count compressed)) (subvec compressed 1)))
      (is (= (compressed-string-type compressed) charset))
      (is (= (compressed-string-type compressed-aligned) charset))
      (is (= (utils/least-greater-multiple (count compressed) 16) (count compressed-aligned))) ))
  (compressed-string-test "" :ascii8)
  (compressed-string-test "1" :ascii8)
  (compressed-string-test "1111111" :ascii8)
  (compressed-string-test "11111111" :ascii7)
  (compressed-string-test "11111111111" :ascii8-zlib)
  (compressed-string-test "český honzík" :utf8)
  (compressed-string-test "český honzík český honzík" :utf8-zlib)
  (compressed-string-test "རེཏེརཏེཏ༢༣༢༤" :utf16)
  (compressed-string-test "རེཏེརཏེཏ༢༣༢༤རེཏེརཏེཏ༢༣༢༤" :utf16-zlib) )

