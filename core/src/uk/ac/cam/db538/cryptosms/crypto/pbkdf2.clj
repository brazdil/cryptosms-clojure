(ns uk.ac.cam.db538.cryptosms.crypto.pbkdf2
  (:use [clojure.test :only (with-test, is, deftest) ]
        [uk.ac.cam.db538.cryptosms.utils :only (HEX) ]
        [uk.ac.cam.db538.cryptosms.charset :only (ASCII8) ])
  (:require [uk.ac.cam.db538.cryptosms.utils :as utils]
            [uk.ac.cam.db538.cryptosms.byte-arrays :as byte-arrays]
            [uk.ac.cam.db538.cryptosms.charset :as charset] )
  (:import (java.security.spec KeySpec)
           (javax.crypto SecretKeyFactory)
           (javax.crypto.spec PBEKeySpec) ))

(defn compute 
"Computes PBKDF2 for a given password, salt, number of iterations and key length, using HmacSHA1."

; private static byte[] pbkdf2(byte[] password, byte[] salt, int rounds, int outputByteCount) 
; throws KeyManagerCryptographyException {

;   //Standard Java PBKDF2 takes the lower 8 bits of each element of a char array as input.
;   //Therefore, rewrite byte array into char array, preserving lower 8-bit pattern
;   char[] charPassword = new char[password.length];
;   for (int i = 0; i < charPassword.length; i++) {
;      if (password[i] >= 0) {
;       charPassword[i] = (char) password[i];
;     } else {
;       charPassword[i] = (char) (password[i] + 256); //cope with signed -> unsigned
;     }
;   }
;   try {
;     SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
;     KeySpec spec = new PBEKeySpec(charPassword, salt, rounds, 8*outputByteCount);
;     return factory.generateSecret(spec).getEncoded();     
;   } catch (NoSuchAlgorithmException e) {
;     throw new KeyManagerCryptographyException(e);
;   } catch (InvalidKeySpecException e) {
;     throw new KeyManagerCryptographyException(e);
;   }
; }  

; general implementation with byte vectors
[ ^ints password  ^ints salt ^Number iterations ^Number key-length ]
(let [ password-length   (count password)
       char-password     (char-array password-length) ]
  ; Standard Java PBKDF2 takes the lower 8 bits of each element of a char array as input.
  ; Therefore, rewrite byte array into char array, preserving lower 8-bit pattern
  (loop [ i 0 ]
    (if (< i password-length)
      (do
        (aset-char char-password i (get password i))
        (recur (inc i)))))
  (let [ factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")
         spec    (new PBEKeySpec char-password (byte-arrays/from-vector salt) iterations (* 8 key-length)) ]
    (byte-arrays/to-vector (. (. factory generateSecret spec) getEncoded)))))

(defn compute-with-charset
  [ ^String password ^String salt ^Number iterations ^Number key-length fn-charset ]
  (compute (fn-charset password) (fn-charset salt) iterations key-length))

(defn compute-utf16
  [ ^String password ^String salt ^Number iterations ^Number key-length ]
  (compute password salt iterations key-length charset/UTF16))

(deftest testPBKDF2
     
     ; Input:
     ;   P = "password" (8 octets)
     ;   S = "salt" (4 octets)
     ;   c = 1
     ;   dkLen = 20
     ; Output:
     ;   DK = 0c 60 c8 0f 96 1f 0e 71
     ;        f3 a9 b5 24 af 60 12 06
     ;        2f e0 37 a6             (20 octets)

     (is (= (compute-with-charset "password" "salt" 1 20 charset/ASCII8) 
            [0x0c 0x60 0xc8 0x0f 0x96 0x1f 0x0e 0x71 0xf3 0xa9 
             0xb5 0x24 0xaf 0x60 0x12 0x06 0x2f 0xe0 0x37 0xa6 ]))

     ; Input:
     ;   P = "password" (8 octets)
     ;   S = "salt" (4 octets)
     ;   c = 2
     ;   dkLen = 20
     ; Output:
     ;   DK = ea 6c 01 4d c7 2d 6f 8c
     ;        cd 1e d9 2a ce 1d 41 f0
     ;        d8 de 89 57             (20 octets)

     (is (= (compute-with-charset "password" "salt" 2 20 charset/ASCII8) 
            [0xea 0x6c 0x01 0x4d 0xc7 0x2d 0x6f 0x8c 0xcd 0x1e 
             0xd9 0x2a 0xce 0x1d 0x41 0xf0 0xd8 0xde 0x89 0x57]))

     ; Input:
     ;   P = "password" (8 octets)
     ;   S = "salt" (4 octets)
     ;   c = 4096
     ;   dkLen = 20
     ; Output:
     ;   DK = 4b 00 79 01 b7 65 48 9a
     ;        be ad 49 d9 26 f7 21 d0
     ;        65 a4 29 c1             (20 octets)

     (is (= (compute-with-charset "password" "salt" 4096 20 charset/ASCII8) 
            [0x4b 0x00 0x79 0x01 0xb7 0x65 0x48 0x9a 0xbe 0xad
             0x49 0xd9 0x26 0xf7 0x21 0xd0 0x65 0xa4 0x29 0xc1]))

     ; Input:
     ;   P = "password" (8 octets)
     ;   S = "salt" (4 octets)
     ;   c = 16777216
     ;   dkLen = 20
     ; Output:
     ;   DK = ee fe 3d 61 cd 4d a4 e4
     ;        e9 94 5b 3d 6b a2 15 8c
     ;        26 34 e9 84             (20 octets)

     (is (= (compute-with-charset "password" "salt" 16777216 20 charset/ASCII8) 
            [0xee 0xfe 0x3d 0x61 0xcd 0x4d 0xa4 0xe4 0xe9 0x94 
             0x5b 0x3d 0x6b 0xa2 0x15 0x8c 0x26 0x34 0xe9 0x84]))

     ; Input:
     ;   P = "passwordPASSWORDpassword" (24 octets)
     ;   S = "saltSALTsaltSALTsaltSALTsaltSALTsalt" (36 octets)
     ;   c = 4096
     ;   dkLen = 25
     ; Output:
     ;   DK = 3d 2e ec 4f e4 1c 84 9b
     ;        80 c8 d8 36 62 c0 e4 4a
     ;        8b 29 1a 96 4c f2 f0 70
     ;        38                      (25 octets)

     (is (= (compute-with-charset "passwordPASSWORDpassword" "saltSALTsaltSALTsaltSALTsaltSALTsalt" 4096 25 charset/ASCII8) 
            [0x3d 0x2e 0xec 0x4f 0xe4 0x1c 0x84 0x9b 0x80 0xc8 
             0xd8 0x36 0x62 0xc0 0xe4 0x4a 0x8b 0x29 0x1a 0x96
             0x4c 0xf2 0xf0 0x70 0x38]))

     ; Input:
     ;   P = "pass\0word" (9 octets) [112 97 115 115 119 111 114 100]
     ;   S = "sa\0lt" (5 octets) [115 97 108 116]
     ;   c = 4096
     ;   dkLen = 16
     ; Output:
     ;   DK = 56 fa 6a a7 55 48 09 9d
     ;        cc 37 d7 f0 34 25 e0 c3 (16 octets)
     (is (= (compute [112 97 115 115 0 119 111 114 100] [115 97 0 108 116] 4096 16) 
            [0x56 0xfa 0x6a 0xa7 0x55 0x48 0x09 0x9d
             0xcc 0x37 0xd7 0xf0 0x34 0x25 0xe0 0xc3]))
)