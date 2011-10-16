(ns uk.ac.cam.db538.cryptosms.zlib
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX ASCII) ])
  (:require [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays] )
  (:import (java.util.zip Inflater Deflater)
           (java.io ByteArrayOutputStream) )) 

(defn deflate
  "Compresses given data (byte vector) with DEFLATE"
  [ ^bytes data ]
  (let [ buffer      (byte-arrays/create 1024)
         deflater    (new Deflater)
         bos         (new ByteArrayOutputStream (count data))
         data-bytes  (byte-arrays/output data) ]
    (. deflater setInput data-bytes)
    (. deflater finish)
    (loop [ ]
      (if (. deflater finished)
        (byte-arrays/input (. bos toByteArray))
        (let [ compressed (. deflater deflate buffer) ]
          (. bos write buffer 0 compressed)
          (recur))))))

(defn inflate
  "Decompresses given data (byte vector) with DEFLATE"
  [ ^bytes data ]
  (let [ buffer      (byte-arrays/create 1024)
         inflater    (new Inflater)
         bos         (new ByteArrayOutputStream (count data))
         data-bytes  (byte-arrays/output data) ]
    (. inflater setInput data-bytes)
    (loop [ ]
      (if (. inflater finished)
        (byte-arrays/input (. bos toByteArray))
        (let [ decompressed (. inflater inflate buffer) ]
          (. bos write buffer 0 decompressed)
          (recur))))))

(with-test
  (defn- test-deflate-inflate [ data ]
    (is (= (inflate (deflate data)) data)))
  (test-deflate-inflate (ASCII "Hello World!"))
  (test-deflate-inflate (ASCII "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ullamcorper tempor nibh ut fringilla. Etiam accumsan ultrices varius. Proin fermentum ligula tempor ipsum tristique eget imperdiet tellus tempus. Morbi quis mauris lorem. Sed in dui ipsum, vel viverra tortor. Suspendisse dapibus nibh ut ante mattis sit amet scelerisque lorem sollicitudin. Maecenas at ipsum sit amet nibh dignissim bibendum. Sed lobortis pharetra sem ut bibendum."))
  (test-deflate-inflate (HEX "00112233445566778899aabbccddeeff")))

