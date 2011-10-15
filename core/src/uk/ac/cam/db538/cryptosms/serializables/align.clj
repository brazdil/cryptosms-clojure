(ns uk.ac.cam.db538.cryptosms.serializables.align
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.crypto.random :as random]
            [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common]
            [uk.ac.cam.db538.cryptosms.serializables.uint :as uint]))

(with-test
  (defn align
    "Returns a serializable type which aligns given serializable to given length."
    [^Number length-aligned exportable]
    (let [ length-random (- length-aligned (:length exportable)) ]
      (if (< length-random 0) ; handles negative alignment length as well
        (throw (new IllegalArgumentException))
        (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
          ; export
          (fn [data] (persistent! (reduce conj! (transient ((:export exportable) data)) (random/rand-next length-random) )))
          ; import
          (fn [^bytes xs]
            (if (not= (count xs) length-aligned)
              (throw (new IllegalArgumentException))
              ((:import exportable) (subvec xs 0 (:length exportable)))))
          ; length
          length-aligned ))))
  (let [ item (uint/uint64 :id)
         data { :id 0x1234567890ABCDEF }
         result [ 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF ]
         result-aligned [ 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF 0xFE 0xDC 0xBA 0x09 0x87 0x65 0x43 0x21 ] ]
    (is (thrown? IllegalArgumentException (align -1 item)))
    (is (thrown? IllegalArgumentException (align 7 item)))
    (is (= (count ((:export (align 128 item)) data)) 128))
    (is (= (subvec ((:export (align 128 item)) data) 0 8 ) result))
    (is (= ((:import (align 16 item)) result-aligned) data))
    (is (= (:length (align 128 item)) 128)) ))

