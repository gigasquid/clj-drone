(ns clj-drone.example.navigation
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all])
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:import (java.lang Float)))




;;; work area not ready for prime time yet

@nav-data
(reset! nav-data {})
(drone-initialize)
(init)
(do 
  (drone-init-navdata)
  (drone-do-for 4 :take-off)
  (drone-do-for 4 :spin-right 0.3)
  (drone :land)
  )
(drone :land)
@nav-data
(end-navstream)
(drone :reset-watchdog)
(drone :emergency)
(drone :flat-trim)



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


(nth navdata 175)
(def state (get-int navdata 4))
(def seq-num (get-int navdata 8))
(def vision-flag (get-int navdata 12))
(def demo-option-header (get-short navdata 16))
(def demo-option-size (get-short navdata 18))
(def demo-ctrl-state (get-int navdata 20))
(def demo-battery (get-int navdata 24))
(def demo-pitch (get-float navdata 28))
(def demo-roll (get-float navdata 32))
(def demo-yaw (get-float navdata 36))
(def demo-altitude (get-int navdata 40))
(def demo-velocity-x (get-int navdata 44))
(def demo-velocity-y (get-int navdata 48))
(def demo-velocity-z (get-int navdata 52))
(def new-offset (+ 16 demo-option-size))
(def vision-detect-option-header (get-short navdata 164))
(def vision-detect-option-size (get-short navdata 166))
(def vision-tag-detected (get-int navdata 168))
(def vision-type (get-int navdata 172))




