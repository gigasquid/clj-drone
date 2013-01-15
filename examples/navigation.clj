(ns clj-drone.example.navigation
  (:use clj-drone.core)
  (:import (java.nio ByteBuffer))
  (:import (java.net DatagramPacket DatagramSocket InetAddress)))


(drone-initialize)
(drone :init-navdata)
(drone :control-ack)

(def nav-datagram-send-packet
  (new DatagramPacket (byte-array (map byte [1 0 0 0])) 1 drone-host 5554))
(def nav-datagram-receive-packet
  (new DatagramPacket (byte-array 4096) 4096 drone-host 5554))
(.setSoTimeout navdata-socket 1000)
(.send navdata-socket nav-datagram-send-packet)
(.receive navdata-socket nav-datagram-receive-packet)
(.length nav-datagram-receive-packet)
(first (.getData nav-datagram-receive-packet)) ;=> -120
(byte-array (map byte [1 0 0 0]))


