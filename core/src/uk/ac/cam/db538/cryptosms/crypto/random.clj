(ns uk.ac.cam.db538.cryptosms.crypto.random
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays])
  (:import (java.security SecureRandom)))

(defn create-random
  "Returns an instance of SecureRandom"
  []
  (SecureRandom/getInstance "SHA1PRNG" "SUN"))

(def global-secure-random (create-random))

(defn rand-next-bytes 
  "Generates a Java byte array of given length, initialized with random data. Uses Java SecureRandom SHA1PRNG."
  [ ^Number len ]
  (locking global-secure-random
    (let [ array (byte-arrays/create len) ]
      (. global-secure-random nextBytes array)
      array)))
  
(with-test
  (defn rand-next 
    "Generates a vector of given length, initialized with random data. Uses Java SecureRandom SHA1PRNG."
    [ ^Number len ]
    (byte-arrays/input (rand-next-bytes len)))
  (is (= (count (rand-next 10)) 10))
  (is (= (rand-next 0) [])))
