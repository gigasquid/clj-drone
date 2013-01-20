(ns clj-drone.example.navigation
  (:use clj-drone.core
        clj-drone.navdata)
  (:import (java.net DatagramPacket DatagramSocket InetAddress)))



@nav-data
(reset! nav-data {})
(drone-initialize)
(drone-init-navdata)
@nav-data
(drone :reset-watchdog)



;;;;;;;;;work area to debug the response


(drone-initialize)
(drone :init-navdata)
(drone :control-ack)

(def nav-datagram-send-packet
  (new DatagramPacket (byte-array (map byte [1 0 0 0])) 1 drone-host 5554)
  )
(def nav-datagram-receive-packet
  (new DatagramPacket (byte-array 2048) 2048 drone-host 5554))
(.setSoTimeout navdata-socket 1000)
(.send navdata-socket nav-datagram-send-packet)
(.receive navdata-socket nav-datagram-receive-packet)
(def navdata (.getData nav-datagram-receive-packet))

navdata
(def state (get-int navdata 4))
(bit-and state 1)


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



