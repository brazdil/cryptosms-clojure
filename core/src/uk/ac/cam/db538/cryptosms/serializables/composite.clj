(ns uk.ac.cam.db538.cryptosms.serializables.composite
  (:use [clojure.test :only (with-test, is) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common]
            [uk.ac.cam.db538.cryptosms.serializables.uint :as uint]))

(with-test
  (defn composite 
    "Returns a serializable type combining together list of other serializables."
    [serializables]
    (let [ composite-length (reduce + 0 (map #(:length %) serializables)) ]
      (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
        ; export
        (fn [data] (persistent! (reduce #(reduce conj! %1 %2) (transient []) (map #((:export %) data) serializables))))
        ; import
        (fn [^bytes xs args] 
          (if (not= (count xs) composite-length)
            (throw (new IllegalArgumentException))
            (let [ offsets (vec (reductions + 0 (map #(:length %) serializables))) ; offsets of items (e.g [ 0 2 6 ] for uin16 and uint32 - last is ignored!!!)
                   ends (subvec offsets 1) ; offsets of following items, e.g [ 2 6 ] as in previous line
                   subvecs (map #(subvec xs %1 %2) offsets ends) ] ; subvectors passed to individual items
              (persistent! (reduce conj! (transient {}) (map #((:import %1) %2 args) serializables subvecs))))))
        ; length
        composite-length )))
    (let [ items [ (uint/uint8 :item1) (uint/uint16 :item2) (uint/uint32 :item3) (uint/uint64 :item4) ]
           data { :item1 0x12, :item2 0x1234, :item3 0x12345678, :item4 0x1234567890ABCDEF} 
           result [ 0x12 0x12 0x34 0x12 0x34 0x56 0x78 0x12 0x34 0x56 0x78 0x90 0xAB 0xCD 0xEF ]
         ]
      (is (thrown? IllegalArgumentException ((:import (composite items)) (vec (range 0 (+ (count result) 1))) {} )))
      (is (thrown? IllegalArgumentException ((:import (composite items)) (vec (range 0 (- (count result) 1))) {} )))
      (is (= ((:export (composite [])) {}) []))
      (is (= ((:import (composite [])) [] {}) {}))
      (is (= (:length (composite [])) 0))
      (is (= ((:export (composite items)) data) result))
      (is (= ((:import (composite items)) result {}) data))
      (is (= (:length (composite items)) (count result))) ))

