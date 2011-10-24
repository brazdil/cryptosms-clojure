(ns uk.ac.cam.db538.cryptosms.crypto.ecdh
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ]
        [uk.ac.cam.db538.cryptosms.charset :only (ASCII8) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] )
  (:import (org.spongycastle.crypto AsymmetricCipherKeyPair)
           (org.spongycastle.crypto.agreement ECDHBasicAgreement)
           (org.spongycastle.crypto.generators ECKeyPairGenerator)
           (org.spongycastle.crypto.params ECDomainParameters ECKeyGenerationParameters ECPrivateKeyParameters ECPublicKeyParameters)
           (org.spongycastle.math.ec ECCurve$Fp ECPoint)
           (org.spongycastle.util.encoders Hex) ))

; curve definition
(def ECDH_P (new BigInteger "0FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF" 16))
(def ECDH_A (new BigInteger "0FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC" 16))
(def ECDH_B (new BigInteger "05AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B" 16))
(def ECDH_N (new BigInteger "0FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551" 16))
(def ECDH_H (BigInteger/valueOf 1))

;(def ECDH_G_X 16r06B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296)
;(def ECDH_G_Y 16r04FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5)
(def ECDH_G (Hex/decode "036B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296"))

(def ECDH_CURVE (new ECCurve$Fp ECDH_P ECDH_A ECDH_B))
(def ECDH_PARAMS (new ECDomainParameters ECDH_CURVE (. ECDH_CURVE decodePoint ECDH_G) ECDH_N ECDH_H))

; global objects
(def global-ecdh-keygen (new ECKeyPairGenerator))

(defn ecdh-generate-private-key []
  (locking random/global-secure-random
    (let [ params-keygen (new ECKeyGenerationParameters ECDH_PARAMS random/global-secure-random) ]
      (locking global-ecdh-keygen
        (. global-ecdh-keygen init params-keygen)
        (let [ key-pair (. global-ecdh-keygen generateKeyPair)
               private-key (cast ECPrivateKeyParameters (. key-pair getPrivate)) ]
          (byte-arrays/input (. (. private-key getD) toByteArray)))))))
          
          
        
