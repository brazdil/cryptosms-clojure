(ns uk.ac.cam.db538.cryptosms.crypto.aes
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.crypto.block-cipher :as block-cipher])
  (:import (org.spongycastle.crypto.engines AESFastEngine)
           (org.spongycastle.crypto.modes CBCBlockCipher)
           (org.spongycastle.crypto.params KeyParameter)
           (org.spongycastle.crypto.params ParametersWithIV)))

(with-test
  (defn encrypt-aes-cbc [ data crypto-key crypto-iv ]
	  (let [ cipher (new CBCBlockCipher (new AESFastEngine))
	         iv-bytes (if (nil? crypto-iv) (random/rand-next-bytes (. cipher getBlockSize)) (byte-arrays/output crypto-iv)) ]
	    (. cipher init true 
	      (new ParametersWithIV 
	        (new KeyParameter (byte-arrays/output crypto-key)) 
	        iv-bytes))
	    (block-cipher/outcome cipher data)))
  (let [ 
         key-128   (HEX "2b7e151628aed2a6abf7158809cf4f3c")
         key-192   (HEX "8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")
         key-256   (HEX "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4")
         data1     (HEX "6bc1bee22e409f96e93d7e117393172a")
         data2     (HEX "ae2d8a571e03ac9c9eb76fac45af8e51")
         data3     (HEX "30c81c46a35ce411e5fbc1191a0a52ef")
         data4     (HEX "f69f2445df4f9b17ad2b417be66c3710")
         iv01      (HEX "000102030405060708090A0B0C0D0E0F")
         iv02      (HEX "7649ABAC8119B246CEE98E9B12E9197D")
         iv03      (HEX "5086CB9B507219EE95DB113A917678B2")
         iv04      (HEX "73BED6B8E3C1743B7116E69E22229516")
         iv05      (HEX "4F021DB243BC633D7178183A9FA071E8")
         iv06      (HEX "B4D9ADA9AD7DEDF4E5E738763F69145A")
         iv07      (HEX "571B242012FB7AE07FA9BAAC3DF102E0")
         iv08      (HEX "F58C4C04D6E5F1BA779EABFB5F7BFBD6")
         iv09      (HEX "9CFC4E967EDB808D679F777BC6702C7D")
         iv10      (HEX "39F23369A9D9BACFA530E26304231461")
       ]
    ; 128-bit, 1 block
    (is (= (encrypt-aes-cbc data1 key-128 iv01) (HEX "7649abac8119b246cee98e9b12e9197d")))
    (is (= (encrypt-aes-cbc data2 key-128 iv02) (HEX "5086cb9b507219ee95db113a917678b2")))
    (is (= (encrypt-aes-cbc data3 key-128 iv03) (HEX "73bed6b8e3c1743b7116e69e22229516")))
    (is (= (encrypt-aes-cbc data4 key-128 iv04) (HEX "3ff1caa1681fac09120eca307586e1a7")))
    ; 192-bit, 1 block
    (is (= (encrypt-aes-cbc data1 key-192 iv01) (HEX "4f021db243bc633d7178183a9fa071e8")))
    (is (= (encrypt-aes-cbc data2 key-192 iv05) (HEX "b4d9ada9ad7dedf4e5e738763f69145a")))
    (is (= (encrypt-aes-cbc data3 key-192 iv06) (HEX "571b242012fb7ae07fa9baac3df102e0")))
    (is (= (encrypt-aes-cbc data4 key-192 iv07) (HEX "08b0e27988598881d920a9e64f5615cd")))
    ; 256-bit, 1 block
    (is (= (encrypt-aes-cbc data1 key-256 iv01) (HEX "f3eed1bdb5d2a03c064b5a7e3db181f8")))
    (is (= (encrypt-aes-cbc data2 key-256 iv08) (HEX "591ccb10d410ed26dc5ba74a31362870")))
    (is (= (encrypt-aes-cbc data3 key-256 iv09) (HEX "b6ed21b99ca6f4f9f153e7b1beafed1d")))
    (is (= (encrypt-aes-cbc data4 key-256 iv10) (HEX "23304b7a39f9f3ff067d8d8f9e24ecc7")))
    ))
