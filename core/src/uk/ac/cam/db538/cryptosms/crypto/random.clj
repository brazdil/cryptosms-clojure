(ns uk.ac.cam.db538.cryptosms.crypto.random
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays])
  (:import (java.security SecureRandom)))

(def secure-random (ref (SecureRandom/getInstance "SHA1PRNG" "SUN")))

(defn rand-next-bytes [ ^Number len ]
  (dosync
    (let [ array (byte-arrays/create len) ]
      (. @secure-random nextBytes array)
      array)))
  
(with-test
  (defn rand-next [ ^Number len ]
      (byte-arrays/input (rand-next-bytes len)))
  (is (= (count (rand-next 10)) 10))
  (is (= (rand-next 0) [])))
