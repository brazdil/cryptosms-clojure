(ns uk.ac.cam.db538.cryptosms.WrongKeyException
  (:gen-class 
    :extends Exception
    :init init
    :constructors { [] []
                    [ String ] [ String ] } 
    :state pos))
   
(defn- -init 
  ; empty constructor
  ( []
  [ [] [] ] )
  ; string message
  ( [msg] 
  [[msg] [] ] ) )
  
