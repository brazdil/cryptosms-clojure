(ns uk.ac.cam.db538.cryptosms.crypto.aes
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.crypto.block-cipher :as block-cipher])
  (:import (org.spongycastle.crypto.engines AESFastEngine)
           (org.spongycastle.crypto.modes CBCBlockCipher)
           (org.spongycastle.crypto.params KeyParameter)
           (org.spongycastle.crypto.params ParametersWithIV)))

(defn encrypt-aes-cbc [ data crypto-key crypto-bytes-iv ]
  (let [ cipher (new CBCBlockCipher (new AESFastEngine)) ]
    (. cipher init true 
      (new ParametersWithIV 
        (new KeyParameter (byte-arrays/output crypto-key)) 
        (random/rand-next-bytes (. cipher getBlockSize))))
    (block-cipher/outcome cipher data)))
