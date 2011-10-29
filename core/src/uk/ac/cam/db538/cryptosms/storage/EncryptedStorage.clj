(ns uk.ac.cam.db538.cryptosms.storage.EncryptedStorage
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random] )
  (:import (java.io File RandomAccessFile)
           (uk.ac.cam.db538.cryptosms CryptoKey) )
  (:gen-class
    :implements [uk.ac.cam.db538.cryptosms.storage.IStorage]
    :state classState
    :init classInit
    :constructors { [String uk.ac.cam.db538.cryptosms.CryptoKey] [] } ))

(defn open-storage
  "Opens or creates given storage file, returns an instance of RandomAccessFile."
  [ #^String filename ]
  (let [ directory (. (new File filename) getParent) ]
    ; create the file's directory (if necessary)
    (if (and (not (= directory nil)) (> (count directory) 0))
      (. (new File directory) mkdirs)) 
    ; open the file and return the object
    (new RandomAccessFile filename "rw")))

(defn -close
  "Closes the encrypted file."
  [ this ]
  (. ((.classState this) :file-object) close) )

(defn -classInit
  "Initializes the EncryptedStorage class. Argument is name of the encrypted file."
  [ filename crypto-key ]
  [ [] { :crypto-key      (byte-arrays/input (. crypto-key getKey))
         :file-object     (open-storage filename) } ] )

(defn -getConversationThread
  [ recipient ]
  nil)
