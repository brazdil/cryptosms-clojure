(defproject cryptosms-core "0.0.1"
  :description "Core part of CryptoSMS project written in Clojure"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :aot [#".*"] 
  :uberjar-name "cryptosms-core-withdeps.jar"
  :javac-options {:destdir "bin/classes/"}
  :java-source-path "3rd-party/src/"
  :repl-init uk.ac.cam.db538.cryptosms.low-level.export)
