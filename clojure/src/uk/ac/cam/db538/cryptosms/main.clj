(ns uk.ac.cam.db538.cryptosms.main)

(gen-class
  :name uk.ac.cam.db538.cryptosms.CTest
  :methods [ [foo [] "[B"]])

(defn -foo [this]
  (byte-array [(byte 1) (byte 2)]))

(gen-interface
  :name uk.ac.cam.db538.cryptosms.ITest
  :methods [ [foo [] "[B"]])
