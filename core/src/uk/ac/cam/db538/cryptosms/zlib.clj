(ns uk.ac.cam.db538.cryptosms.zlib
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ]
        [uk.ac.cam.db538.cryptosms.charset :only (ASCII8) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] )
  (:import (java.util.zip Inflater Deflater DataFormatException)
           (java.io ByteArrayOutputStream) )) 

(defn deflate
  "Compresses given data (byte vector) with DEFLATE"
  [ ^bytes data ]
  (let [ buffer      (byte-arrays/create 1024)
         deflater    (new Deflater)
         bos         (new ByteArrayOutputStream (count data))
         data-bytes  (byte-arrays/from-vector data) ]
    (. deflater setInput data-bytes)
    (. deflater finish)
    (loop [ ]
      (if (. deflater finished)
        (subvec (byte-arrays/to-vector (. bos toByteArray)) 2) ; get rid of ZLIB header - wastes 2 bytes
        (let [ compressed (. deflater deflate buffer) ]
          (. bos write buffer 0 compressed)
          (recur))))))

(defn inflate
  "Decompresses given data (byte vector) with DEFLATE"
  [ ^bytes data ]
  (if (< (count data) 6)
    (throw (new IllegalArgumentException)) ; too short
    (let [ buffer      (byte-arrays/create 1024)
           inflater    (new Inflater)
           bos         (new ByteArrayOutputStream (count data))
           data-bytes  (byte-arrays/from-vector (persistent! (reduce conj! (transient [ 120 156 ]) data)) ) ] ; prepends ZLIB header
      (. inflater setInput data-bytes)
      (loop [ ]
        (if (. inflater finished)
          (byte-arrays/to-vector (. bos toByteArray))
          (let [ decompressed (. inflater inflate buffer) ]
            (. bos write buffer 0 decompressed)
            (recur)))))))

(with-test
  (defn- test-deflate-inflate [ data ]
    (is (= (inflate (deflate data)) data)))
  (test-deflate-inflate (ASCII8 "Hello World!"))
  (test-deflate-inflate (ASCII8 "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ullamcorper tempor nibh ut fringilla. Etiam accumsan ultrices varius. Proin fermentum ligula tempor ipsum tristique eget imperdiet tellus tempus. Morbi quis mauris lorem. Sed in dui ipsum, vel viverra tortor. Suspendisse dapibus nibh ut ante mattis sit amet scelerisque lorem sollicitudin. Maecenas at ipsum sit amet nibh dignissim bibendum. Sed lobortis pharetra sem ut bibendum."))
  (test-deflate-inflate (HEX "00112233445566778899aabbccddeeff"))
  (is (thrown? IllegalArgumentException (inflate [ ])))
  (is (thrown? IllegalArgumentException (inflate [ 1 ])))
  (is (thrown? IllegalArgumentException (inflate [ 1 2 3 4 5 ])))
  (is (thrown? DataFormatException (inflate [ 1 2 3 4 5 6 ]))) )

