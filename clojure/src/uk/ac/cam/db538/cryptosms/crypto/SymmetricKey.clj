(ns uk.ac.cam.db538.cryptosms.crypto.SymmetricKey
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]))
  
(gen-class
  :name           uk.ac.cam.db538.cryptosms.crypto.SymmetricKey
  :state          state
  :init           init
  :constructors   { [#=(utils/type-byte-array)] [] }
  :methods        [ [getBytes [] #=(utils/type-byte-array) ] ])

;; constructor
;; take a byte array and just store it inside
(defn -init [key]
  [[] key])

;; retrieve the key from state
(defn -getBytes [this]
  (. (.state this) clone))
