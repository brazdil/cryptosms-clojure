(ns uk.ac.cam.db538.cryptosms.charset
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays] ))

(defn ASCII8 
  "Given a string, returns byte-vector with 8-bit ASCII representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "US-ASCII"))
    (if (vector? data)
      (new String (byte-arrays/output data) "US-ASCII")
      (throw (new IllegalArgumentException)))))

(defn UTF8 
  "Given a string, returns byte-vector with UTF-8 representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "UTF-8"))
    (if (vector? data)
      (new String (byte-arrays/output data) "UTF-8")
      (throw (new IllegalArgumentException)))))

(defn UTF16 
  "Given a string, returns byte-vector with UTF-16 representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (byte-arrays/input (. data getBytes "UTF-16"))
    (if (vector? data)
      (new String (byte-arrays/output data) "UTF-16")
      (throw (new IllegalArgumentException)))))

(with-test
  (defn- length-in-ascii7 
    "Returns length of string if represented as 7-bit ASCII. Argument is number of characters (length of the string)."
    [ ^Number len ]
    (utils/round-up-div (* len 7) 8))
  (is (= (length-in-ascii7 0) 0))
  (is (= (length-in-ascii7 1) 1))
  (is (= (length-in-ascii7 7) 7))
  (is (= (length-in-ascii7 8) 7))
  (is (= (length-in-ascii7 14) 13))
  (is (= (length-in-ascii7 15) 14))
  (is (= (length-in-ascii7 16) 14))
  (is (= (length-in-ascii7 30) 27))
  (is (= (length-in-ascii7 31) 28))
  (is (= (length-in-ascii7 32) 28)))
      
(defn ASCII7 
  "Given a string, returns byte-vector with 7-bit ASCII representation of that string.
   Given a vector, does the reverse."
  [ data ]
  (if (string? data)
    (let [ ascii (ASCII8 data) ]
      ascii)
    (if (vector? data)
      (ASCII8 data))))

