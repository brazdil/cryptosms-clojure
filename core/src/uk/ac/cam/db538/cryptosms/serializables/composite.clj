(ns uk.ac.cam.db538.cryptosms.serializables.composite
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common]
            [uk.ac.cam.db538.cryptosms.serializables.uint :as uint]))

(with-test
  (defn create 
    "Returns a serializable type combining together list of other serializables."
    [serializables]
    (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
      ; export
      (fn [data] (reduce #(reduce conj %1 %2) (vector-of :int) (map #((:export %) data) serializables)))
      ; import
      (fn [^bytes xs args]
        (loop [ offset 0
                serializables serializables
                accu (transient {}) ]
          (if (empty? serializables)
            (persistent! accu)
            (let [ serializable-this (serializables 0)
                   result ((:import serializable-this) (subvec xs offset) args) ]
              (recur (+ offset ((:length serializable-this) result)) (subvec serializables 1) (conj! accu result))))))
        ; length
        (fn [data] (reduce + 0 (map #((:length %) data) serializables))) ))
    (let [ items [ (uint/uint8 :item1) (uint/uint16 :item2) (uint/uint32 :item3) (uint/uint64 :item4) ]
           data { :item1 0x12, :item2 0x1234, :item3 0x12345678, :item4 0x1234567890ABCDEF} 
           result [ 0x12 0x12 0x34 0x12 0x34 0x56 0x78 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF ]
         ]
      (is (thrown? IllegalArgumentException ((:import (create items)) (vec (range 0 (- (count result) 1))) {} )))
      (is (= ((:export (create [])) {}) []))
      (is (= ((:import (create [])) [] {}) {}))
      (is (= ((:length (create [])) {}) 0 ))
      (is (= ((:export (create items)) data) result))
      (is (= ((:import (create items)) result {}) data))
      (is (= ((:length (create items)) data) (count result))) ))

