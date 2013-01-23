(ns clj-drone.example.navigation
  (:use clj-drone.core
        clj-drone.navdata)
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:import (java.lang Float)))



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


(nth navdata 163)
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
(def new-offset (+ 16 demo-option-size))
(def vision-detect-option-header (get-short navdata 164))

(+ (bit-shift-left (bit-and (nth navdata 19)  0x000000FF) 8)
  (bit-and (nth navdata 18) 0x000000FF))
(+ 16 (- (/ demo-option-size 4) 4))
(+ 28 4) (* 32 4) (/ 148 4)
(* 10 4)
(+ 40 4)
(+ 2 2 4 4 4 4 4 4 4 4 4 4 )
(/ (Float/intBitsToFloat (Integer. (get-int navdata 28))) 1000)
(bit-shift-right demo-ctrl-state 16)
vision-flag
(def x (map byte [102 3 0 0]))
(get-int x 0)
(get-int (take 4 navdata) 0)
seq-num
state
(parse-navdata navdata)
(range 4)
(new Float "12.0")
(Float/intBitsToFloat 10)



