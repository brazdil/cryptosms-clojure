(ns uk.ac.cam.db538.cryptosms.crypto.ICryptography
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]))

(gen-interface
  :name       uk.ac.cam.db538.cryptosms.crypto.ICryptography
  :methods    [
                [encryptAES_CBC [#=(utils/type-byte-array), uk.ac.cam.db538.cryptosms.crypto.SymmetricKey] #=(utils/type-byte-array)]
              ])
