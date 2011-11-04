(ns uk.ac.cam.db538.cryptosms.serializables.raw-data
  (:use [clojure.test :only (with-test, is) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.string-compression :as string-compression]
            [uk.ac.cam.db538.cryptosms.serializables.common :as common] ) )

(with-test
  (defn create
    "Returns a serializable type that stores raw data (byte vector)."
    [key-name len]
    (uk.ac.cam.db538.cryptosms.serializables.common.Serializable.
      ; export
      (fn [data]
        (if (not= (count (data key-name)) len)
          (throw (new IllegalArgumentException))
          (data key-name)))
      ; import
      (fn [^bytes xs args]
        (if (< (count xs) len)
          (throw (new IllegalArgumentException))
          {key-name (subvec xs 0 len)} ))
      ; length
      (fn [data] len) ))
  (let [ data                   {:id [ 1 2 3 4 5 ]}
         data-long              {:id [ 1 2 3 4 5 6 ]}
         data-short             {:id [ 1 2 3 4 ]}
         serializable           (create :id 5)
         exported               ((:export serializable) data)
         imported               ((:import serializable) exported {}) ]
    (is (= data imported))
    (is (= (count exported) 5))
    (is (= ((:length serializable) data) 5 ))
    (is (thrown? IllegalArgumentException ((:export serializable) data-long)))
    (is (thrown? IllegalArgumentException ((:export serializable) data-short)))
    (is (thrown? IllegalArgumentException ((:import serializable) [ 1 2 3 4 ] {})))
    (is (= data ((:import serializable) [ 1 2 3 4 5 6] {}) ))
    (is (= data ((:import serializable) [ 1 2 3 4 5 7] {}) )) ))


