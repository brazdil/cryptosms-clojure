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
                        #^String           journal
                        #^Integer          transaction-depth
                                           transaction-data
                                           error-callback])
                                           
(defrecord BinaryFileChange [ #^Integer index 
                              #^"[B"    data ])

(def entry-length 256)

(def journal-type-change    0)
(def journal-type-end       1)
(def journal-type-ok        2)
(def journal-bytes-end      (BinaryFileChange. 0 (random/next-bytes 256)))
(def journal-bytes-ok       (BinaryFileChange. 0 (random/next-bytes 256)))

(def journal-structure
  (composite/create [
    (uint/uint8 :type)
    (uint/uint32 :index) ]))
    
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

(defn- #^RandomAccessFile safe-create-empty
  "If the given file already exists, it deletes it first and the calls 
   safe-open to create it."
  [ #^String filename ]
  (let [ file (new File filename) ]
    ; create the file's directory (if necessary)
    (if (. file exists)
      (. file delete))
    ; open the file and return the object
    (safe-open filename)))

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
                            file-journal
                            0 ; initial depth zero
                            [] ; initial data
                            error-callback)) ]
    (set-error-handler! file-agent error-handler-func )
    file-agent))

(defn- write-to-binary-file
  "Write single entry into the binary file."
  [ #^RandomAccessFile binary-file #^BinaryFileChange change ]
  (let [ offset         (* (:index change) entry-length)
         file-length    (. binary-file length) ]
    ; check the index is valid
    (if (> offset file-length)
      (throw (new StorageFileException "Entry index out of bounds"))
      (do
        ; move to the right spot
        (. binary-file seek offset)
        ; write the data
        (. binary-file write (:data change))))))

(defn- write-multiple-to-binary-file 
  "Write multiple entries into the binary file."
  [ #^RandomAccessFile binary-file changes ]
  (loop [ changes changes ]
    (if (not (empty? changes))
      (do 
        (write-to-binary-file binary-file (changes 0))
        (recur (subvec changes 1))))))

(defn- write-to-journal-file
  "Write single entry into the journal file."
  [ #^RandomAccessFile journal #^Integer entry-type #^BinaryFileChange change ]
  (let [ header (byte-arrays/from-vector 
                  ((:export journal-structure) 
                    {:type entry-type 
                     :index (:index change)})) ]
    ; write the header
    (. journal write header)
    ; write the data
    (. journal write (:data change))))

(defn- write-multiple-to-journal-file 
  "Write multiple entries into the journal file."
  [ #^RandomAccessFile journal #^Integer entry-type changes ]
  (loop [ changes changes ]
    (if (not (empty? changes))
      (do 
        (write-to-journal-file journal entry-type (changes 0))
        (recur (subvec changes 1))))))

(defn- write-entries
  "Writes transaction changes into journal. They already should be 
   checked for length (in transaction-change)"
  [ #^BinaryFile file ^Boolean delete-when-done]
  (let [ #^RandomAccessFile journal (safe-create-empty (:journal file))
         #^RandomAccessFile binary-file (:storage file)
         changes (:transaction-data file) ]
    (if (> (count changes) 0)
      (do 
        ; write all the changes to journal
        (write-multiple-to-journal-file journal journal-type-change changes)
        ; write END
        (write-to-journal-file journal journal-type-end journal-bytes-end)
        ; write change into binary file
        (write-multiple-to-binary-file binary-file changes)
        ; write OK
        (write-to-journal-file journal journal-type-ok journal-bytes-ok)
        ; close journal
        (. journal close)
        ; delete journal
        (if delete-when-done
          (. (new File (:journal file)) delete))
        ; return nil
    ))))

(with-test
  (defn- transaction-start
    "Called on the BinaryFile agent. Should just increase the depth of 
     transaction."
    [ #^BinaryFile file ]
    (assoc file :transaction-depth (+ 1 (:transaction-depth file))))
  (let [ file-before (BinaryFile. nil nil 10 [] nil)
         file-after  (BinaryFile. nil nil 11 [] nil) ]
    (is (= (transaction-start file-before) file-after))))
    
(with-test
  (defn- transaction-end
    "Called on the BinaryFile agent. Should decrease the depth of transaction
     and if the new value is zero, write changed data to binary file and 
     journal."
    [ #^BinaryFile file ]
    (let [ new-depth (- (:transaction-depth file) 1) ]
      (if (> new-depth 0)
        ; still inside transaction
        (assoc file :transaction-depth new-depth)
        ; end of transaction
        (do 
          (write-entries file false)
          (assoc file :transaction-depth 0 :transaction-data [])))))
  (let [ file-before (BinaryFile. nil nil 10 [] nil)
         file-after  (BinaryFile. nil nil 9 [] nil) ]
    (is (= (transaction-end file-before) file-after))))

(with-test
  (defn- replace-or-add [ data ^BinaryFileChange change ]
    (if (some #(= (:index change) (:index %)) data)
      (map 
        #(if (= (:index change) (:index %))
          change
          %)
        data)
      (conj data change)))
  (let [ data123 (byte-arrays/from-vector [ 1 2 3 ])
         data456 (byte-arrays/from-vector [ 4 5 6 ])
         data789 (byte-arrays/from-vector [ 7 8 9 ]) ]
  (let [ data-before [ ]
         change        (BinaryFileChange. 1 data789)
         data-after  [ (BinaryFileChange. 1 data789) ] ]
    (is (= (replace-or-add data-before change) data-after)))
  (let [ data-before [ (BinaryFileChange. 1 data123) 
                       (BinaryFileChange. 2 data456) ]
         change        (BinaryFileChange. 1 data789)
         data-after  [ (BinaryFileChange. 1 data789) 
                       (BinaryFileChange. 2 data456) ] ]
    (is (= (replace-or-add data-before change) data-after)))
  (let [ data-before [ (BinaryFileChange. 1 data123) 
                       (BinaryFileChange. 2 data456) ]
         change        (BinaryFileChange. 3 data789)
         data-after  [ (BinaryFileChange. 1 data123) 
                       (BinaryFileChange. 2 data456)
                       (BinaryFileChange. 3 data789) ] ]
    (is (= (replace-or-add data-before change) data-after)))))

(defn- transaction-change
  "Called on the BinaryFile agent. Should put the BinaryFileChange object
   into the pending data vector inside BinaryFile."
  [ #^BinaryFile file #^BinaryFileChange change ]
  (assoc file :transaction-data 
    (replace-or-add (:transaction-data file) change)))

(with-test
  (defn change
    "Schedules a change to be written into the journal and binary file. Has
     to be called from inside of a binary file transaction (macro wrapping
     around dosync).
     Arguments: 
       - BinaryFile agent
       - file entry index
       - byte vector with data"
    [ #^clojure.lang.Agent file-agent #^Integer index #^clojure.core.Vec data ]
    (if (not= (count data) entry-length)
      (throw (new IllegalArgumentException "Wrong length of data"))
      (send-off file-agent transaction-change (BinaryFileChange. index (byte-arrays/from-vector data)))))
  (let [ change-short (random/next-vector 255)
         change-long  (random/next-vector 257)
         change-ok    (random/next-vector 256)
         test-file    (agent (BinaryFile. nil nil 0 [] nil)) ]
    (change test-file 1 change-ok)
    (is (thrown? IllegalArgumentException (change test-file 1 change-short)))
    (is (thrown? IllegalArgumentException (change test-file 1 change-long)))))

(defmacro transaction [ #^clojure.lang.Agent file-agent & operations ]
  `(dosync
    (send-off ~file-agent transaction-start)
    ~@operations
    (send-off ~file-agent transaction-end)))

(defn- safe-await [the-agent]
  (try
    (await-for 200 the-agent)
    (catch java.lang.Throwable ex)))

(deftest test-create-storage-file
  ; creates file and journal in different, non-existing folders in tmpdir
  (let [ tmpdir        (System/getProperty "java.io.tmpdir")
         dir           (str tmpdir "/cryptosms" (rand-int 1000000) "/binary/file/test")
         file-storage  (str dir "/storage/file")
         file-journal  (str dir "/journal/file2.dat")
         callback-atom (atom nil)
         callback      #(reset! callback-atom %1)
         file-agent    (open file-storage file-journal callback) ]
    ; storage file exists
    (is (. (new File file-storage) exists))
    ; length is zero
    (is (= (. (new File file-storage) length) 0))))
    
(defn- create-test-file 
  []
  (let [ tmpdir       (System/getProperty "java.io.tmpdir")
         dir          (str tmpdir "/cryptosms" (rand-int 1000000) "/binary/file/test")
         file-storage (str dir "/storage/file")
         file-journal (str dir "/journal/file2.dat")
         callback-atom (atom nil)
         callback      #(reset! callback-atom %1) ]
    {:file-agent (open file-storage file-journal callback) :callback-atom callback-atom } ))


