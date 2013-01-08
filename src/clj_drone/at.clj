(ns clj-drone.at
  (:import (java.util BitSet)
           (java.nio ByteBuffer))
  (:use clj-drone.commands))

(defn build-command-int [command-bit-vec]
  (reduce #(bit-set %1 %2) 0 command-bit-vec))

(defn cast-float-to-int [f]
  (let [bbuffer (ByteBuffer/allocate 4)]
    (.put (.asFloatBuffer bbuffer) 0 f)
    (.get (.asIntBuffer bbuffer) 0)))


(defn build-ref-command [command-key counter]
  (let [{:keys [command-class command-bit-vec]} (command-key commands)]
    (str command-class
         "="
         counter
         ","
         (build-command-int command-bit-vec)
         "\r")))

(defn build-pcmd-command [command-key counter val]
  (let [{:keys [command-class command-vec dir] or {dir 1}} (command-key commands)
         ival (if val (* dir (cast-float-to-int val)) 1)]
    (str command-class
         "="
         counter
         ","
         (apply str (interpose "," (replace {:x ival} command-vec)))
         "\r")))

(defn build-command [command-key counter & [val]]
  (let [{:keys [command-class]} (command-key commands)]
    (case command-class
      "AT*REF"  (build-ref-command command-key counter)
      "AT*PCMD" (build-pcmd-command command-key counter val)
      :else     (throw (Exception. "Unsupported Drone Command")))))
