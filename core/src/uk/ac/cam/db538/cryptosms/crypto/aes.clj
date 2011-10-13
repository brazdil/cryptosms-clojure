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

; encrypt/decrypt tested both together below

(def cipher-aes-cbc (new CBCBlockCipher (new AESFastEngine)))
(def block-size-aes-cbc (. cipher-aes-cbc getBlockSize))

(defn encrypt-aes-cbc [ data crypto-key crypto-iv ]
  (let [ iv-bytes (if (nil? crypto-iv) (random/rand-next-bytes block-size-aes-cbc) (byte-arrays/output crypto-iv)) ]
    (. cipher-aes-cbc reset)
    (. cipher-aes-cbc init true 
      (new ParametersWithIV 
        (new KeyParameter crypto-key)
        iv-bytes))
    (block-cipher/outcome cipher-aes-cbc data)))

(defn decrypt-aes-cbc [ data crypto-key crypto-iv ]
  (let [ iv-bytes (byte-arrays/output crypto-iv) ]
    (. cipher-aes-cbc reset)
    (. cipher-aes-cbc init false 
      (new ParametersWithIV 
        (new KeyParameter crypto-key)
        iv-bytes))
    (block-cipher/outcome cipher-aes-cbc data)))

(with-test
  (defn- test-aes-cbc [ data crypto-key crypto-iv result ]
    (let [ crypto-key-bytes (byte-arrays/output crypto-key)]
      (is (= (encrypt-aes-cbc data crypto-key-bytes crypto-iv) result))
      (is (= (decrypt-aes-cbc result crypto-key-bytes crypto-iv) data))))
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
    (test-aes-cbc data1 key-128 iv01 (HEX "7649abac8119b246cee98e9b12e9197d"))
    (test-aes-cbc data2 key-128 iv02 (HEX "5086cb9b507219ee95db113a917678b2"))
    (test-aes-cbc data3 key-128 iv03 (HEX "73bed6b8e3c1743b7116e69e22229516"))
    (test-aes-cbc data4 key-128 iv04 (HEX "3ff1caa1681fac09120eca307586e1a7"))
    ; 192-bit, 1 block
    (test-aes-cbc data1 key-192 iv01 (HEX "4f021db243bc633d7178183a9fa071e8"))
    (test-aes-cbc data2 key-192 iv05 (HEX "b4d9ada9ad7dedf4e5e738763f69145a"))
    (test-aes-cbc data3 key-192 iv06 (HEX "571b242012fb7ae07fa9baac3df102e0"))
    (test-aes-cbc data4 key-192 iv07 (HEX "08b0e27988598881d920a9e64f5615cd"))
    ; 256-bit, 1 block
    (test-aes-cbc data1 key-256 iv01 (HEX "f58c4c04d6e5f1ba779eabfb5f7bfbd6"))
    (test-aes-cbc data2 key-256 iv08 (HEX "9cfc4e967edb808d679f777bc6702c7d"))
    (test-aes-cbc data3 key-256 iv09 (HEX "39f23369a9d9bacfa530e26304231461"))
    (test-aes-cbc data4 key-256 iv10 (HEX "b2eb05e2c39be9fcda6c19078c6a9d1b")) )
  ; 128-bit, 1 block, changing data
  (let [
         key-128 (HEX "00000000000000000000000000000000") 
         iv      (HEX "00000000000000000000000000000000")
       ]
    (test-aes-cbc (HEX "f34481ec3cc627bacd5dc3fb08f273e6") key-128 iv (HEX "0336763e966d92595a567cc9ce537f5e"))
    (test-aes-cbc (HEX "9798c4640bad75c7c3227db910174e72") key-128 iv (HEX "a9a1631bf4996954ebc093957b234589"))
    (test-aes-cbc (HEX "96ab5c2ff612d9dfaae8c31f30c42168") key-128 iv (HEX "ff4f8391a6a40ca5b25d23bedd44a597"))
    (test-aes-cbc (HEX "6a118a874519e64e9963798a503f1d35") key-128 iv (HEX "dc43be40be0e53712f7e2bf5ca707209"))
    (test-aes-cbc (HEX "cb9fceec81286ca3e989bd979b0cb284") key-128 iv (HEX "92beedab1895a94faa69b632e5cc47ce"))
    (test-aes-cbc (HEX "b26aeb1874e47ca8358ff22378f09144") key-128 iv (HEX "459264f4798f6a78bacb89c15ed3d601"))
    (test-aes-cbc (HEX "58c8e00b2631686d54eab84b91f0aca1") key-128 iv (HEX "08a4e2efec8a8e3312ca7460b9040bbf")))
  ; 256-bit, 1 block, changing data
  (let [
         key-256 (HEX "0000000000000000000000000000000000000000000000000000000000000000") 
         iv      (HEX "00000000000000000000000000000000")
       ]
    (test-aes-cbc (HEX "014730f80ac625fe84f026c60bfd547d") key-256 iv (HEX "5c9d844ed46f9885085e5d6a4f94c7d7"))
    (test-aes-cbc (HEX "0b24af36193ce4665f2825d7b4749c98") key-256 iv (HEX "a9ff75bd7cf6613d3731c77c3b6d0c04"))
    (test-aes-cbc (HEX "761c1fe41a18acf20d241650611d90f1") key-256 iv (HEX "623a52fcea5d443e48d9181ab32c7421"))
    (test-aes-cbc (HEX "8a560769d605868ad80d819bdba03771") key-256 iv (HEX "38f2c7ae10612415d27ca190d27da8b4"))
    (test-aes-cbc (HEX "91fbef2d15a97816060bee1feaa49afe") key-256 iv (HEX "1bc704f1bce135ceb810341b216d7abe")) )
 ; 256-bit, 1 block, changing key
 (let [
        data    (HEX "00000000000000000000000000000000")
        iv      (HEX "00000000000000000000000000000000")
      ]
   (test-aes-cbc data (HEX "c47b0294dbbbee0fec4757f22ffeee3587ca4730c3d33b691df38bab076bc558") iv (HEX "46f2fb342d6f0ab477476fc501242c5f"))
   (test-aes-cbc data (HEX "28d46cffa158533194214a91e712fc2b45b518076675affd910edeca5f41ac64") iv (HEX "4bf3b0a69aeb6657794f2901b1440ad4"))
   (test-aes-cbc data (HEX "c1cc358b449909a19436cfbb3f852ef8bcb5ed12ac7058325f56e6099aab1a1c") iv (HEX "352065272169abf9856843927d0674fd"))
   (test-aes-cbc data (HEX "984ca75f4ee8d706f46c2d98c0bf4a45f5b00d791c2dfeb191b5ed8e420fd627") iv (HEX "4307456a9e67813b452e15fa8fffe398"))
   (test-aes-cbc data (HEX "b43d08a447ac8609baadae4ff12918b9f68fc1653f1269222f123981ded7a92f") iv (HEX "4663446607354989477a5c6f0f007ef4"))
   (test-aes-cbc data (HEX "1d85a181b54cde51f0e098095b2962fdc93b51fe9b88602b3f54130bf76a5bd9") iv (HEX "531c2c38344578b84d50b3c917bbb6e1"))
   (test-aes-cbc data (HEX "dc0eba1f2232a7879ded34ed8428eeb8769b056bbaf8ad77cb65c3541430b4cf") iv (HEX "fc6aec906323480005c58e7e1ab004ad"))
   (test-aes-cbc data (HEX "f8be9ba615c5a952cabbca24f68f8593039624d524c816acda2c9183bd917cb9") iv (HEX "a3944b95ca0b52043584ef02151926a8"))
   (test-aes-cbc data (HEX "797f8b3d176dac5b7e34a2d539c4ef367a16f8635f6264737591c5c07bf57a3e") iv (HEX "a74289fe73a4c123ca189ea1e1b49ad5"))
   (test-aes-cbc data (HEX "6838d40caf927749c13f0329d331f448e202c73ef52c5f73a37ca635d4c47707") iv (HEX "b91d4ea4488644b56cf0812fa7fcf5fc"))
   (test-aes-cbc data (HEX "ccd1bc3c659cd3c59bc437484e3c5c724441da8d6e90ce556cd57d0752663bbc") iv (HEX "304f81ab61a80c2e743b94d5002a126b"))
   (test-aes-cbc data (HEX "13428b5e4c005e0636dd338405d173ab135dec2a25c22c5df0722d69dcc43887") iv (HEX "649a71545378c783e368c9ade7114f6c"))
   (test-aes-cbc data (HEX "07eb03a08d291d1b07408bf3512ab40c91097ac77461aad4bb859647f74f00ee") iv (HEX "47cb030da2ab051dfc6c4bf6910d12bb"))
   (test-aes-cbc data (HEX "90143ae20cd78c5d8ebdd6cb9dc1762427a96c78c639bccc41a61424564eafe1") iv (HEX "798c7c005dee432b2c8ea5dfa381ecc3"))
   (test-aes-cbc data (HEX "b7a5794d52737475d53d5a377200849be0260a67a2b22ced8bbef12882270d07") iv (HEX "637c31dc2591a07636f646b72daabbe7"))
   (test-aes-cbc data (HEX "fca02f3d5011cfc5c1e23165d413a049d4526a991827424d896fe3435e0bf68e") iv (HEX "179a49c712154bbffbe6e7a84a18e220")) )
 ; 128-bit, 2 blocks
 (let [ 
        key-128    (HEX "c286696d887c9aa0611bbb3e2025a45a")
        data       (HEX "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")
        iv         (HEX "562e17996d093d28ddb3ba695a2e6f58")
        result     (HEX "d296cd94c2cccf8a3a863028b5e1dc0a7586602d253cfff91b8266bea6d61ab1")
      ]
   (test-aes-cbc data key-128 iv result))
 ; 128-bit, 3 blocks
 (let [ 
        key-128    (HEX "6c3ea0477630ce21a2ce334aa746c2cd")
        data       (HEX "5468697320697320612034382d62797465206d657373616765202865786163746c7920332041455320626c6f636b7329")
        iv         (HEX "c782dc4c098c66cbd9cd27d825682c81")
        result     (HEX "d0a02b3836451753d493665d33f0e8862dea54cdb293abc7506939276772f8d5021c19216bad525c8579695d83ba2684")
      ]
   (test-aes-cbc data key-128 iv result))
 ; 128-bit, 4 blocks
 (let [ 
        key-128    (HEX "56e47a38c5598974bc46903dba290349")
        data       (HEX "a0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedf")
        iv         (HEX "8ce82eefbea0da3c44699ed7db51b7d9")
        result     (HEX "c30e32ffedc0774e6aff6af0869f71aa0f3af07a9a31a9c684db207eb0ef8e4e35907aa632c3ffdf868bb7b29d3d46ad83ce9f9a102ee99d49a53e87f4c3da55")
      ]
   (test-aes-cbc data key-128 iv result)) )
