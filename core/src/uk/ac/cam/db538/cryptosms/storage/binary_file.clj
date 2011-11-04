(ns uk.ac.cam.db538.cryptosms.storage.binary-file
  (:use [clojure.test :only (with-test, is, deftest) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.serializables.align :as align]
            [uk.ac.cam.db538.cryptosms.serializables.composite :as composite]
            [uk.ac.cam.db538.cryptosms.serializables.uint :as uint]
            [uk.ac.cam.db538.cryptosms.serializables.raw-data :as raw-data] )
  (:import (java.io File RandomAccessFile)
           (uk.ac.cam.db538.cryptosms.storage StorageFileException) ))

(defrecord BinaryFile [ #^RandomAccessFile storage
                        #^RandomAccessFile journal
                        error-callback])

(def entry-length 256)
(def journal-length 256)

(defn- error-handler-func 
  "Will be called when an exception is thrown in either of the files. 
   Calls the callback method given to agent."
  [the-agent the-err]
  ((:error-callback @the-agent) the-err))
  
(defn- #^RandomAccessFile safe-open
  "If the directory for file doesn't exist, attempts to create it first, and 
   then opens the file and returns the RandomAccessFile object."
  [ #^String filename ]
  (let [ directory (. (new File filename) getParent) ]
    ; create the file's directory (if necessary)
    (if (and (not (= directory nil)) (> (count directory) 0))
      (. (new File directory) mkdirs))
    ; open the file and return the object
    (new RandomAccessFile filename "rw")))

(defn #^clojure.lang.Agent open
  "Opens or creates given storage file, returns an instance of BinaryFile
   structure wrapped in an agent.
   Arguments:
     - path to the storage file
     - path to the journal file
     - callback function in case of exception"
  [ #^String file-storage #^String file-journal error-callback ]
  (let [ file-agent     (agent 
                          (BinaryFile. 
                            (safe-open file-storage)
                            (safe-open file-journal)
                            error-callback)) ]
    (set-error-handler! file-agent error-handler-func )
    file-agent))

(defn- write-entry-func
  [ #^BinaryFile file #^Integer index #^clojure.core.Vec data ]
  (if (not= (count data) entry-length)
    (throw (new StorageFileException "Illegal data length"))
    (let [ offset         (* index entry-length)
           file-length    (. (:storage file) length) ]
      (if (> offset file-length)
        (throw (new StorageFileException "Entry offset out of bounds"))
        
        file))))

(defn write-entry
  "Writes an entry into the storage file (and journal)
   Arguments:
     - BinaryFile agent
     - entry index
     - data to be written (256-byte vector)"
  [ #^clojure.lang.Agent file-agent #^Integer index #^clojure.core.Vec data ]
  (send-off file-agent write-entry-func index data))

(defn- safe-await [the-agent]
  (try
    (await-for 200 the-agent)
    (catch java.lang.Throwable ex)))

(deftest test-create-file
  ; creates file and journal in different, non-existing folders in tmpdir
  (let [ tmpdir        (System/getProperty "java.io.tmpdir")
         dir           (str tmpdir "/cryptosms" (rand-int 1000000) "/binary/file/test")
         file-storage  (str dir "/storage/file")
         file-journal  (str dir "/journal/file2.dat")
         callback-atom (atom nil)
         callback      #(reset! callback-atom %1)
         file-agent    (open file-storage file-journal callback) ]
    ; both files exist
    (is (. (new File file-storage) exists))
    (is (. (new File file-journal) exists))
    ; their length is zero
    (is (= (. (new File file-storage) length) 0))
    (is (= (. (new File file-journal) length) 0))))
    
(defn- create-test-file 
  []
  (let [ tmpdir       (System/getProperty "java.io.tmpdir")
         dir          (str tmpdir "/cryptosms" (rand-int 1000000) "/binary/file/test")
         file-storage (str dir "/storage/file")
         file-journal (str dir "/journal/file2.dat")
         callback-atom (atom nil)
         callback      #(reset! callback-atom %1) ]
    {:file-agent (open file-storage file-journal callback) :callback-atom callback-atom } ))

(deftest test-writing-out-of-bounds
  (let [ test-file (create-test-file) ]
    ; writing into index 0 should be fine
    (is (nil? @(:callback-atom test-file)))
    (write-entry (:file-agent test-file) 0 (random/next-vector entry-length))
    (safe-await (:file-agent test-file))
    (is (nil? @(:callback-atom test-file)))
    ; writing into index 2 should NOT be fine, callback should contain the exception
    (write-entry (:file-agent test-file) 2 (random/next-vector entry-length))
    (safe-await (:file-agent test-file))
    (is (not (nil? @(:callback-atom test-file))))))

(deftest test-writing-data-length
  (let [ test-file (create-test-file) ]
    ; writing 256 bytes into index 0 should be fine
    (is (nil? @(:callback-atom test-file)))
    (write-entry (:file-agent test-file) 0 (random/next-vector entry-length))
    (safe-await (:file-agent test-file))
    (is (nil? @(:callback-atom test-file)))
    ; writing 255 bytes into index 0 should be fine
    (write-entry (:file-agent test-file) 0 (random/next-vector (- entry-length 1)))
    (safe-await (:file-agent test-file))
    (is (not (nil? @(:callback-atom test-file))))))

