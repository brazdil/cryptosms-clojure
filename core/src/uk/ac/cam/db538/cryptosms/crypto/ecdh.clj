(ns uk.ac.cam.db538.cryptosms.crypto.ecdh
  (:use [clojure.test :only (with-test, is, deftest) ]
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
           (org.spongycastle.util.encoders Hex)
           (java.security SecureRandom) ))

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
(def ECDH_RANDOM (random/create))
(def ECDH_KEYGEN_PARAMS (new ECKeyGenerationParameters ECDH_PARAMS ECDH_RANDOM))

; global objects
(def global-ecdh-keygen 
  (doto (new ECKeyPairGenerator)
    (.init ECDH_KEYGEN_PARAMS)))

(def length-ecdh-key 33)

(defn generate-private-key 
  "Generates a new random private key for ECDH."
  []
  (locking global-ecdh-keygen
    (let [ key-pair (. global-ecdh-keygen generateKeyPair)
           private-key (cast ECPrivateKeyParameters (. key-pair getPrivate))
           private-key-vector (byte-arrays/to-vector (. (. private-key getD) toByteArray)) ]
      ; align key vector to length-ecdh-key with leading zeros
      (reduce conj (reduce conj (vector-of :int) (repeat (- length-ecdh-key (count private-key-vector)) 0)) private-key-vector) )))
          
(defn get-public-key 
  "Takes an ECDH private key and returns the associated public key to be sent
   to the other party."
  [ private-key ]
  (locking global-ecdh-keygen
    (let [ key-pair (. global-ecdh-keygen createKeyPair (new BigInteger (byte-arrays/from-vector private-key)) )
           public-key (cast ECPublicKeyParameters (. key-pair getPublic)) ]
      (byte-arrays/to-vector (. (. (. public-key getQ) getCompressed) getEncoded)))))

(defn get-shared-key
  "Takes an ECDH private key and a public key of the other party and returns
   the shared key they both should compute."
  [ private-key other-public-key ]
  (locking global-ecdh-keygen
    (locking ECDH_CURVE
      (locking ECDH_PARAMS
        (let [ key-pair (. global-ecdh-keygen createKeyPair (new BigInteger (byte-arrays/from-vector private-key)) )
               point (. ECDH_CURVE decodePoint (byte-arrays/from-vector other-public-key))
               agreement (doto (new ECDHBasicAgreement)
                           (.init (. key-pair getPrivate))) ]
          (. agreement calculateAgreement (new ECPublicKeyParameters point ECDH_PARAMS)))))))

(defn- ecdh-test [ prv-key1 pub-key1 prv-key2 pub-key2 shared ]
  (let [ prv-key1 (byte-arrays/to-vector (. (new BigInteger prv-key1) toByteArray))
         prv-key2 (byte-arrays/to-vector (. (new BigInteger prv-key2) toByteArray))
         pub-key1 (new BigInteger pub-key1)
         pub-key2 (new BigInteger pub-key2)
         shared (new BigInteger shared)
         pub-key-cmp1 (get-public-key prv-key1) 
         pub-key-cmp2 (get-public-key prv-key2)
         pub-key-int1 (new BigInteger (byte-arrays/from-vector (reduce conj [0] (subvec pub-key-cmp1 1))))
         pub-key-int2 (new BigInteger (byte-arrays/from-vector (reduce conj [0] (subvec pub-key-cmp2 1)))) ]
    (is (= pub-key-int1 pub-key1))
    (is (= pub-key-int2 pub-key2))
    (is (= (get-shared-key prv-key1 pub-key-cmp2) shared))
    (is (= (get-shared-key prv-key2 pub-key-cmp1) shared)) ))

(deftest ecdh-tests
  (ecdh-test 
    "51441801928236716010013635591552581846128449845150279427262442831051560169685"  ; Alice private
    "49035340809787831484513009929069148940983235547512289260756773206087085339886"  ; Alice public
    "126994045756865999350722444340940071413149806944750798112885796887257874018"    ; Bob private
    "24729845806285838529778224653987544879868930571714720793309138464353093601615"  ; Bob public
    "11580158922589983153080425842872259040961772709978641539821598786450308167787") ; Shared secret 
  (ecdh-test 
    "78586538164349096460176574543380547590713718246042661635633757430839851925360"  ; Alice private
    "105441533581469678361561091895976479703983660903690720250555552093567242927000" ; Alice public
    "45157684999065374361036473822898214667211368998391107185374156036020967588877"  ; Bob private
    "31682149481118806299227757368298896428427940317903103261888302901709756053729"  ; Bob public
    "61830103149895215790584829089557388398330770309375426769069776059494790486775") ; Shared secret 
  (ecdh-test 
    "51653925047948772303159346661354830416802120420111366547175067386836131968364"  ; Alice private
    "49580007320997211429416488570318974337021713284177166098290263388688138316280"  ; Alice public
    "30299854624833508876021819950053799466342189497147755264973866169840824229901"  ; Bob private
    "66990695845067559695126086216658800365697003534398164091046680021441221078972"  ; Bob public
    "41025872958014154736786020212226025631628785600823319662937419944313606428924") ; Shared secret 
  (dotimes [n 1000] 
    (let [ prv-key (generate-private-key) ]
      (is (= (count prv-key) length-ecdh-key))
      (is (= (count (get-public-key prv-key)) length-ecdh-key)))))
    
