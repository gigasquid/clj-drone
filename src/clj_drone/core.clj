(ns clj-drone.core
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:use clj-drone.at))

(def default-drone-ip "192.168.1.1")
(def default-at-port 5556)
(def default-navdata-port 5554)

(declare drone)

(defn drone-initialize
  ([] (drone-initialize default-drone-ip default-at-port))
  ([ip port]
    (def drone-host (InetAddress/getByName ip))
    (def at-port port)
    (def at-socket (DatagramSocket. ))
    (def counter (atom 0))
    (drone :flat-trim)))

(defn send-command [data]
  (.send at-socket
    (new DatagramPacket (.getBytes data) (.length data) drone-host at-port)))

(defn drone [command-key & [w x y z]]
  (let [ seq-num (swap! counter inc)
         data (build-command command-key seq-num w x y z)]
    (send-command data)))

(defn drone-do-for [seconds command-key & [w x y z]]
  (when (> seconds 0)
    (drone command-key w x y z)
    (Thread/sleep 30)
    (drone-do-for (- seconds 0.03) command-key w x y z)))
