(ns uk.ac.cam.db538.cryptosms.crypto.hash
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ]
        [uk.ac.cam.db538.cryptosms.charset :only (ASCII8) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] )
  (:import (org.spongycastle.crypto.digests SHA256Digest) ))

(def global-hash-sha256 (new SHA256Digest))
(def length-hash-sha256 (locking global-hash-sha256 (. global-hash-sha256 getDigestSize)))

(with-test
  (defn sha256 
    "Returns SHA-256 hash of given data. Data is a vector with uint8 elements."
    [ data ]
    (locking global-hash-sha256
      (let [ data-bytes    (byte-arrays/from-vector data)
             data-length   (count data) 
             result-bytes  (byte-arrays/create length-hash-sha256) ]
        (. global-hash-sha256 reset)
        (. global-hash-sha256 update data-bytes 0 data-length)
        (. global-hash-sha256 doFinal result-bytes 0)
        (byte-arrays/into-vector result-bytes))))
  (is (= (sha256 (ASCII8 "The quick brown fox jumps over the lazy dog") ) (HEX "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592")))
  (is (= (sha256 (ASCII8 "The quick brown fox jumps over the lazy dog.") ) (HEX "ef537f25c895bfa782526529a9b63d97aa631564d5d789c2b765448c8635fb6c")))
  ; from NESSIE test vectors
  (is (= (sha256 (ASCII8 "")) (HEX "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855")))
  (is (= (sha256 (ASCII8 "a")) (HEX "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB")))
  (is (= (sha256 (ASCII8 "abc")) (HEX "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD")))
  (is (= (sha256 (ASCII8 "message digest")) (HEX "F7846F55CF23E14EEBEAB5B4E1550CAD5B509E3348FBC4EFA3A1413D393CB650")))
  (is (= (sha256 (ASCII8 "abcdefghijklmnopqrstuvwxyz")) (HEX "71C480DF93D6AE2F1EFAD1447C66C9525E316218CF51FC8D9ED832F2DAF18B73")))
  (is (= (sha256 (ASCII8 "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq")) (HEX "248D6A61D20638B8E5C026930C3E6039A33CE45964FF2167F6ECEDD419DB06C1")))
  (is (= (sha256 (ASCII8 "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")) (HEX "DB4BFCBD4DA0CD85A60C3C37D3FBD8805C77F15FC6B1FDFE614EE0A7C8FDB4C0")))
  (is (= (sha256 (ASCII8 "12345678901234567890123456789012345678901234567890123456789012345678901234567890")) (HEX "F371BC4A311F2B009EEF952DD83CA80E2B60026C8E935592D0F9C308453C813E"))) )

