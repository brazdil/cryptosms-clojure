(ns uk.ac.cam.db538.cryptosms.crypto.ICryptography
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]))

(gen-interface
  :name uk.ac.cam.db538.cryptosms.crypto.ICryptography
  :methods [ [encryptAES [#=(utils/type-byte-array), #=(utils/type-byte-array)] #=(utils/type-byte-array)] ])
