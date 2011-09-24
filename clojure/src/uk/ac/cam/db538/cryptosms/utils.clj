(ns uk.ac.cam.db538.cryptosms.utils)

;; debugging parts of expressions
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

;; byte-array Java type
(defn type-byte-array[] (java.lang.Class/forName "[B"))
