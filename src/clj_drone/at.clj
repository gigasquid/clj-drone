(ns clj-drone.at
  (:import (java.util BitSet)
           (java.nio ByteBuffer))
  (:require [clj-drone.commands :refer :all]))

(defn build-command-int [command-bit-vec]
  (reduce #(bit-set %1 %2) 0 command-bit-vec))

(defn cast-float-to-int [f]
  (let [bbuffer (ByteBuffer/allocate 4)]
    (.put (.asFloatBuffer bbuffer) 0 f)
    (.get (.asIntBuffer bbuffer) 0)))

(defn build-base-command [command-class counter]
  (str command-class "=" counter ","))

(defn build-ref-command [command-key counter]
  (let [{:keys [command-class command-bit-vec]} (command-key commands)]
    (str (build-base-command command-class counter)
         (build-command-int command-bit-vec)
         "\r")))

(defn build-pcmd-command [command-key counter & [[v w x y]]]
  (let [{:keys [command-class command-vec dir] or {dir 1}} (command-key commands)
        v-val (when v (cast-float-to-int (* dir v)))
        w-val (when w (cast-float-to-int w))
        x-val (when x (cast-float-to-int x))
        y-val (when y (cast-float-to-int y))]
    (str (build-base-command command-class counter)
         (apply str (interpose "," (replace {:v v-val :w w-val :x x-val :y y-val} command-vec)))
         "\r")))

(defn build-simple-command [command-key counter]
  (let [{:keys [command-class value]} (command-key commands)]
    (str (build-base-command command-class counter)
         value
         "\r")))

(defn build-config-command [command-key counter]
  (let [{:keys [command-class option value]} (command-key commands)]
    (str (build-base-command command-class counter)
         (apply str (interpose "," [option, value]))
         "\r")))

(defn build-command [command-key counter & values]
  (let [{:keys [command-class]} (command-key commands)]
    (case command-class
      "AT*REF"  (build-ref-command command-key counter)
      "AT*PCMD" (build-pcmd-command command-key counter values)
      "AT*FTRIM" (build-simple-command command-key counter)
      "AT*COMWDG" (build-simple-command command-key counter)
      "AT*CONFIG" (build-config-command command-key counter)
      "AT*CTRL" (build-simple-command command-key counter)
      :else     (throw (Exception. "Unsupported Drone Command")))))
