(ns uk.ac.cam.db538.cryptosms.crypto.hmac
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ]
        [uk.ac.cam.db538.cryptosms.charset :only (ASCII8) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.crypto.hash :as hash] )
  (:import (org.spongycastle.crypto.macs HMac)
           (org.spongycastle.crypto.digests SHA1Digest SHA256Digest)
           (org.spongycastle.crypto.params KeyParameter) ))

; HMAC-SHA-1

(def global-hmac-sha1 (new HMac (new SHA1Digest)))
(def length-sha1 (locking global-hmac-sha1 (. global-hmac-sha1 getMacSize)))

(with-test
  (defn sha1
    "Returns a HMAC-SHA-1 for given data and key. Data is a vector, key is Java byte-array."
    [ data crypto-key ]
    (locking global-hmac-sha1
      (let [ data-bytes    (byte-arrays/output data) 
             data-length   (count data)
             result-bytes  (byte-arrays/create length-sha1) ]
        (. global-hmac-sha1 reset)
        (. global-hmac-sha1 init (new KeyParameter crypto-key))
        (. global-hmac-sha1 update data-bytes 0 data-length)
        (. global-hmac-sha1 doFinal result-bytes 0)
        (byte-arrays/input result-bytes))))
  ; tests from http://tools.ietf.org/html/rfc2202
  (is (= (sha1
           (ASCII8 "Hi There")
           (byte-arrays/output (HEX "0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b")))
         (HEX "b617318655057264e28bc0b6fb378c8ef146be00")))
  (is (= (sha1
           (ASCII8 "what do ya want for nothing?")
           (byte-arrays/output (ASCII8 "Jefe")))
         (HEX "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79")))
  (is (= (sha1
           (vec (repeat 50 0xdd))
           (byte-arrays/output (HEX "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))
         (HEX "125d7342b9ac11cd91a39af48aa17b4f63f175d3")))
  (is (= (sha1
           (vec (repeat 50 0xcd))
           (byte-arrays/output (HEX "0102030405060708090a0b0c0d0e0f10111213141516171819")))
         (HEX "4c9007f4026250c6bc8414f9bf50c86c2d7235da")))
  (is (= (sha1
           (ASCII8 "Test With Truncation")
           (byte-arrays/output (HEX "0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c")))
         (HEX "4c1a03424b55e07fe7f27be1d58bb9324a9a5a04")))
  (is (= (sha1
           (ASCII8 "Test Using Larger Than Block-Size Key - Hash Key First")
           (byte-arrays/output (vec (repeat 80 0xaa))))
         (HEX "aa4ae5e15272d00e95705637ce8a3b55ed402112")))
  (is (= (sha1
           (ASCII8 "Test Using Larger Than Block-Size Key and Larger Than One Block-Size Data")
           (byte-arrays/output (vec (repeat 80 0xaa))))
         (HEX "e8e99d0f45237d786d6bbaa7965c7808bbff1a91"))) )

; HMAC-SHA-256

(def global-hmac-sha256 (new HMac (new SHA256Digest)))
(def length-sha256 (locking global-hmac-sha256 (. global-hmac-sha256 getMacSize)))

(with-test
  (defn sha256 
    "Returns a HMAC-SHA-256 for given data and key. Data is a vector, key is Java byte-array."
    [ data crypto-key ]
    (locking global-hmac-sha256
      (let [ data-bytes    (byte-arrays/output data) 
             data-length   (count data)
             result-bytes  (byte-arrays/create length-sha256) ]
        (. global-hmac-sha256 reset)
        (. global-hmac-sha256 init (new KeyParameter crypto-key))
        (. global-hmac-sha256 update data-bytes 0 data-length)
        (. global-hmac-sha256 doFinal result-bytes 0)
        (byte-arrays/input result-bytes))))
  ; tests from http://tools.ietf.org/html/rfc4231
  (is (= (sha256 
           (HEX "4869205468657265") 
           (byte-arrays/output (HEX "0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b"))) 
         (HEX "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7")))
  (is (= (sha256 
           (HEX "7768617420646f2079612077616e7420666f72206e6f7468696e673f") 
           (byte-arrays/output (HEX "4a656665"))) 
         (HEX "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843")))
  (is (= (sha256 
           (HEX "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd") 
           (byte-arrays/output (HEX "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))) 
         (HEX "773ea91e36800e46854db8ebd09181a72959098b3ef8c122d9635514ced565fe")))
  (is (= (sha256
           (HEX "cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd") 
           (byte-arrays/output (HEX "0102030405060708090a0b0c0d0e0f10111213141516171819"))) 
         (HEX "82558a389a443c0ea4cc819899f2083a85f0faa3e578f8077a2e3ff46729665b")))
  (is (= (subvec (sha256 
                   (HEX "546573742057697468205472756e636174696f6e") 
                   (byte-arrays/output (HEX "0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c"))) 0 16) 
         (HEX "a3b6167473100ee06e0c796c2955552b")))
  (is (= (sha256 
           (HEX "54657374205573696e67204c6172676572205468616e20426c6f636b2d53697a65204b6579202d2048617368204b6579204669727374") 
           (byte-arrays/output (HEX "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))
         (HEX "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54")))
  (is (= (sha256 
           (HEX "5468697320697320612074657374207573696e672061206c6172676572207468616e20626c6f636b2d73697a65206b657920616e642061206c6172676572207468616e20626c6f636b2d73697a6520646174612e20546865206b6579206e6565647320746f20626520686173686564206265666f7265206265696e6720757365642062792074686520484d414320616c676f726974686d2e") 
           (byte-arrays/output (HEX "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))
         (HEX "9b09ffa71b942fcb27635fbcd5b0e944bfdc63644f0713938a7f51535c3a35e2"))))

