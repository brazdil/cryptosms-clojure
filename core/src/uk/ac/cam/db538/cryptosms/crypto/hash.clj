(ns uk.ac.cam.db538.cryptosms.crypto.hash
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX ASCII) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays] )
  (:import (org.spongycastle.crypto.digests SHA256Digest) ))

(def digest-sha256 (ref (new SHA256Digest)))
(def length-sha256 (dosync (. @digest-sha256 getDigestSize)))

(with-test
  (defn hash-sha256 [ data ]
    (dosync
      (let [ data-bytes    (byte-arrays/output data)
             data-length   (count data) 
             result-bytes  (byte-arrays/create length-sha256) ]
        (. @digest-sha256 reset)
        (. @digest-sha256 update data-bytes 0 data-length)
        (. @digest-sha256 doFinal result-bytes 0)
        (byte-arrays/input result-bytes))))
  (is (= (hash-sha256 (ASCII "The quick brown fox jumps over the lazy dog") ) (HEX "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592")))
  (is (= (hash-sha256 (ASCII "The quick brown fox jumps over the lazy dog.") ) (HEX "ef537f25c895bfa782526529a9b63d97aa631564d5d789c2b765448c8635fb6c")))
  ; from NESSIE test vectors
  (is (= (hash-sha256 (ASCII "")) (HEX "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855")))
  (is (= (hash-sha256 (ASCII "a")) (HEX "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB")))
  (is (= (hash-sha256 (ASCII "abc")) (HEX "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD")))
  (is (= (hash-sha256 (ASCII "message digest")) (HEX "F7846F55CF23E14EEBEAB5B4E1550CAD5B509E3348FBC4EFA3A1413D393CB650")))
  (is (= (hash-sha256 (ASCII "abcdefghijklmnopqrstuvwxyz")) (HEX "71C480DF93D6AE2F1EFAD1447C66C9525E316218CF51FC8D9ED832F2DAF18B73")))
  (is (= (hash-sha256 (ASCII "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq")) (HEX "248D6A61D20638B8E5C026930C3E6039A33CE45964FF2167F6ECEDD419DB06C1")))
  (is (= (hash-sha256 (ASCII "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")) (HEX "DB4BFCBD4DA0CD85A60C3C37D3FBD8805C77F15FC6B1FDFE614EE0A7C8FDB4C0")))
  (is (= (hash-sha256 (ASCII "12345678901234567890123456789012345678901234567890123456789012345678901234567890")) (HEX "F371BC4A311F2B009EEF952DD83CA80E2B60026C8E935592D0F9C308453C813E"))) )

