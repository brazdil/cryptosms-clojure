(ns uk.ac.cam.db538.cryptosms.serializables.compressed-string
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.string-compression :as string-compression]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common] ) )

(with-test
  (defn compressed-string
    "Returns a serializable type that stores String using the best possible
     combination of encoding and DEFLATE compression, such that the size
     is least possible and no data is lost."
    [key-name]
    (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
      ; export
      (fn [data]
        (string-compression/compress (data key-name)))
      ; import
      (fn [^bytes xs args] 
        (if (< (count xs) 1)
          (throw (new IllegalArgumentException))
          {key-name (string-compression/decompress xs)} ))
      ; length
      (fn [data] (count (string-compression/compress (data key-name)))) ))
  ; tests just check that everything is integrated together correctly... string compression methods are tested separately
  (let [ data                   {:string "Hello world! I'm CryptoSMS"}
         serializable           (compressed-string :string)
         exported               ((:export serializable) data)
         imported               ((:import serializable) exported {}) ]
    (is (= data imported))
    (is (= (count exported) 24))
    (is (= ((:length serializable) data) 24 ))))

(with-test
  (defn compressed-string-aligned
    "Returns a serializable type that stores String using the best possible
     combination of encoding and DEFLATE compression, such that the size
     is least possible and no data is lost. Data is aligned to 16 bytes
     (useful for encryption)."
    [key-name]
    (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
      ; export
      (fn [data]
        (string-compression/compress-align (data key-name)))
      ; import
      (fn [^bytes xs args] 
        (if (< (count xs) 16)
          (throw (new IllegalArgumentException))
          {key-name (string-compression/decompress-aligned xs)} ))
      ; length
      (fn [data] (count (string-compression/compress-align (data key-name)))) ))
  ; tests just check that everything is integrated together correctly... string compression methods are tested separately
  (let [ data                   {:string "Hello world! I'm CryptoSMS"}
         serializable           (compressed-string-aligned :string)
         exported               ((:export serializable) data)
         imported               ((:import serializable) exported {}) ]
    (is (= data imported))
    (is (= (count exported) 32))
    (is (= ((:length serializable) data) 32 ))))
