(ns uk.ac.cam.db538.cryptosms.crypto.block-cipher
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils])
  (:require [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays]))

(defn outcome 
  "Returns the outcome of given BouncyCastle BlockCipher, which was previously set up."
  [ cipher data ]
  (let [ block-size        (. cipher getBlockSize)
         length-expected   (utils/least-greater-multiple (count data) block-size)
         data-result       (byte-arrays/create length-expected)
         data-bytes        (byte-arrays/output data) ]
    (loop [ off 0 ]
      (if (>= off length-expected)
        (byte-arrays/input data-result)
        (if (not= (. cipher processBlock data-bytes off data-result off) block-size)
          nil ; error
          (recur (+ off block-size)))))));
