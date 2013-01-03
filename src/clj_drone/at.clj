(ns clj-drone.at
  (:import (java.util BitSet))
  (:use clj-drone.commands))

(defn build-command-int [command-bit-vec]
  (reduce #(bit-set %1 %2) 0 command-bit-vec))

(defn build-command [command-key counter]
  (let [{:keys [command-class command-bit-vec]} (command-key commands)]
    (str
      command-class
      "="
      counter
      ","
      (build-command-int command-bit-vec)
      "\r")))