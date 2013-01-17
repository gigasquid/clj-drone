(ns clj-drone.example.navigation
  (:use clj-drone.core
        clj-drone.navdata)
  (:require [bytebuffer.buff :as bb] )
  (:import (java.nio ByteBuffer))
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  )


(drone-initialize)
(drone :init-navdata)
(drone :control-ack)

(def nav-datagram-send-packet
  (new DatagramPacket (byte-array (map byte [1 0 0 0])) 1 drone-host 5554)
  )
(def nav-datagram-receive-packet
  (new DatagramPacket (byte-array 4096) 4096 drone-host 5554))
(.setSoTimeout navdata-socket 1000)
(.send navdata-socket nav-datagram-send-packet)
(.receive navdata-socket nav-datagram-receive-packet)
(def navdata (.getData nav-datagram-receive-packet))
(first navdata)
(count navdata) => 4096
()
(first (.getData nav-datagram-receive-packet)) ;=> -120
(byte-array (map byte [1 0 0 0]))


(def x [1 2 3 4])
(take 3 x)
(def header (take 32 navdata))
header
header
navdata

(/ 4096 8)
(def mybuff (bb/byte-buffer 4096))
(.put mybuff navdata)
(.flip mybuff)
(def header (bb/take-int mybuff))
(def drone-state (bb/take-int mybuff))
drone-state
header
(class (take 4 navdata))


(def bold (ByteBuffer/allocate 4096))
bold
(.put bold navdata)
(.flip bold)
(def header-bytes (byte-array 4))
(def hb (.get bold header-bytes 0 4))
(bb/take-int hb)
(.get hb 0)
(first (.array hb))
(.get (.asIntBuffer (.get bold header-bytes 0 4)) 0)  ; sb 1432778632


(bit-and (nth header-bytes 0)  0x000000FF)
;;; yes works
(+
  (bit-shift-left (bit-and (nth navdata 0) 0x000000FF) (* 8 0))
  (bit-shift-left (bit-and (nth navdata 1) 0x000000FF) (* 8 1))
  (bit-shift-left (bit-and (nth navdata 2) 0x000000FF) (* 8 2))
  (bit-shift-left (bit-and (nth navdata 3) 0x000000FF) (* 8 3))
  )

(let [c 0x000000FF]
  (reduce
    #(+ %1 (bit-shift-left (bit-and (nth navdata %2) c) (* 8 %2)))
    0
    [0 1 2 3]))

(get-int-from-bytes navdata 0)



