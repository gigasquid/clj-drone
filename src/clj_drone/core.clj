(ns clj-drone.core
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:use clj-drone.at))

(def default-drone-ip "192.168.1.1")
(def default-at-port 5556)

(defn drone-initialize
  ([] (drone-initialize default-drone-ip default-at-port))
  ([ip port]
    (def drone-host (InetAddress/getByName ip))
    (def at-port port)
    (def socket (DatagramSocket. ))
    (def counter (atom 0))))

(defn send-command [data]
  (.send socket
    (new DatagramPacket (.getBytes data) (.length data) drone-host at-port)))

(defn drone [command-key & [val]]
  (let [ seq-num (swap! counter inc)
         data (build-command command-key seq-num val)]
    (.send socket
      (new DatagramPacket (.getBytes data) (.length data) drone-host at-port))))

(defn drone-do-for [seconds command-key & [val]]
  (when (> seconds 0)
    (drone command-key val)
    (Thread/sleep 30)
    (drone-do-for (- seconds 0.03) command-key val)))
