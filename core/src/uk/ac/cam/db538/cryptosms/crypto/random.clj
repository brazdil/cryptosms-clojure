(ns uk.ac.cam.db538.cryptosms.crypto.random
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.low-level.byte-arrays :as byte-arrays])
  (:import (java.security SecureRandom)))

(def secure-random (SecureRandom/getInstance "SHA1PRNG" "SUN"))

(with-test
  (defn rand-next [ ^Number len ]
    (let [ array (byte-array (repeat len (byte 0))) ]
      (. secure-random nextBytes array)
      (byte-arrays/input array)))
  (is (= (count (rand-next 100)) 100))
  (is (= (rand-next 0) [])))
