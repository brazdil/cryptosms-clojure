(ns uk.ac.cam.db538.cryptosms.serializables.crypto
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ])
  (:require [uk.ac.cam.db538.cryptosms.WrongKeyException]
            [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common]
            [uk.ac.cam.db538.cryptosms.serializables.align :as align]
            [uk.ac.cam.db538.cryptosms.serializables.uint :as uint]
            [uk.ac.cam.db538.cryptosms.crypto.aes :as aes]
            [uk.ac.cam.db538.cryptosms.crypto.hmac :as hmac]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays] ) )

(def overhead-aes-cbc-sha1 (+ hmac/length-hmac-sha1 aes/block-size-aes-cbc))

(with-test
  (defn aes-cbc-sha1 
    "Returns a serializable type encrypting/decrypting data (other serializable) with AES/CBC/HMAC-SHA1 
     under given crypto key (Java byte array stored in passed in data/args).
     Crypto key can be 128, 192 or 256 bits long."
    [key-name serializable]
    (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
      ; export
      (fn [data]
        (let [ length-data-aligned    (utils/least-greater-multiple ((:length serializable) data) aes/block-size-aes-cbc) 
               serializable-aligned   (align/align length-data-aligned serializable)
               length-data-all        (+ overhead-aes-cbc-sha1 length-data-aligned) ; HMAC + IV + data
               serialized-data        ((:export serializable-aligned) data)
               crypto-key             (key-name data)
               crypto-iv              (random/rand-next aes/block-size-aes-cbc) ]
          (reduce conj
            (hmac/hmac-sha1 serialized-data crypto-key)
              (reduce conj
                crypto-iv
                (aes/encrypt-aes-cbc serialized-data crypto-key crypto-iv)))))
      ; import
      (fn [^bytes xs args] 
        (if (< (count xs) overhead-aes-cbc-sha1)
          (throw (new IllegalArgumentException))
          (let [ crypto-key      (key-name args)
                 data-hmac       (subvec xs 0 hmac/length-hmac-sha1)
                 data-iv         (subvec xs hmac/length-hmac-sha1 overhead-aes-cbc-sha1)
                 data-encrypted  (subvec xs overhead-aes-cbc-sha1)
                 data-decrypted  (aes/decrypt-aes-cbc data-encrypted crypto-key data-iv)
                 hmac-expected   (hmac/hmac-sha1 data-decrypted crypto-key) ]
            (if (= data-hmac hmac-expected)
              ((:import serializable) data-decrypted args)
              (throw (new uk.ac.cam.db538.cryptosms.WrongKeyException))))))
      ; length
      (fn [data] (+ overhead-aes-cbc-sha1 (utils/least-greater-multiple ((:length serializable) data) aes/block-size-aes-cbc))) ))
  ; tests just check that the functions conjugate/cut everything correctly... crypto algorithms are tested separately
  (let [ data                   {:id64 0x01234567890ABCDEF}
         crypto-serializable    (aes-cbc-sha1 :crypto-key (uint/uint64 :id64))
         crypto-args            {:crypto-key (byte-arrays/output (HEX "00112233445566778899AABBCCDDEEFF"))}
         crypto-args-wrong      {:crypto-key (byte-arrays/output (HEX "00112233445566778899AABBCCDDEEF0"))}
         crypto-data            (conj data crypto-args)
         exported               ((:export crypto-serializable) crypto-data)
         imported               ((:import crypto-serializable) exported crypto-args) ]
    (is (= data imported))
    (is (thrown? uk.ac.cam.db538.cryptosms.WrongKeyException ((:import crypto-serializable) exported crypto-args-wrong)))
    (is (= (count exported) 52))
    (is (= ((:length crypto-serializable) data) 52 ))))
