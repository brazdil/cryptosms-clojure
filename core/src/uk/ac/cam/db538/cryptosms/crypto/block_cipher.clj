(ns uk.ac.cam.db538.cryptosms.crypto.block-cipher
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils])
  (:require [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays]))

(defn outcome [ cipher data ]
  (let [ length-expected (utils/least-greater-multiple (count data) (. cipher getBlockSize))
         data-result (byte-arrays/create length-expected)
         data-bytes (byte-arrays/output data)
         length-actual (. cipher processBlock data-bytes 0 data-result 0) ]
    (if (= length-expected length-actual)
      (byte-arrays/input data-result))))