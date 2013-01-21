(ns clj-drone.example.navigation
  (:use clj-drone.core
        clj-drone.navdata)
  (:import (java.net DatagramPacket DatagramSocket InetAddress)))



@nav-data
(reset! stop-navstream false )
(reset! nav-data {})
(drone-initialize)
(drone-init-navdata)
(drone :take-off)
(drone :land)
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


(nth navdata 11)
(def state (get-int navdata 4))
(def seq-num (get-int navdata 8))
(def x (map byte [102 3 0 0]))
(get-int x 0)
seq-num
state



