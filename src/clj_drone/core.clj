(ns clj-drone.core
  (require [ clj-logging-config.log4j :as log-config]
           [ clojure.tools.logging :as log])
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:require [clj-drone.at :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))


(def default-drone-ip "192.168.1.1")
(def default-at-port 5556)
(def default-navdata-port 5554)
(def at-socket (DatagramSocket. ))
(def navdata-socket (DatagramSocket. ))
(def counter (atom 0))
(declare drone)

(defn init-logger []
  (log-config/set-logger! :level :debug
                          :out (org.apache.log4j.FileAppender.
                                (org.apache.log4j.EnhancedPatternLayout. org.apache.log4j.EnhancedPatternLayout/TTCC_CONVERSION_PATTERN)
                                "logs/drone.log"
                                true)))

(defn drone-initialize
  ([] (drone-initialize default-drone-ip default-at-port default-navdata-port))
  ([ip at-port navdata-port]
     (def drone-host (InetAddress/getByName ip))
     (def at-port at-port)
     (def navdata-port navdata-port)
     (do
       (reset! counter 0)
       (drone :flat-trim))))

(defn send-command
  ([data] (send-command data at-socket))
  ([data socket]
     (.send socket
            (new DatagramPacket (.getBytes data) (.length data) drone-host at-port))))

(defn drone [command-key & [w x y z]]
  (let [ seq-num (swap! counter inc)
        data (build-command command-key seq-num w x y z)]
    (send-command data)))

(defn drone-do-for [seconds command-key & [w x y z]]
  (when (> seconds 0)
    (drone command-key w x y z)
    (Thread/sleep 30)
    (drone-do-for (- seconds 0.03) command-key w x y z)))

(defn drone-stop-navdata []
  (reset! stop-navstream true))

(defn communication-check []
  (when (= :problem (@nav-data :com-watchdog))
    (log/info "Watchdog Reset")
    (drone :reset-watchdog)))

(defn stream-navdata [socket packet]
  (do
    (receive-navdata socket packet)
    (parse-navdata (get-navdata-bytes packet))
    (log/info (str "navdata: "(log-flight-data)))
    (log/info "hey")
    (communication-check)
    (eval-current-goals @nav-data)
    (log/info (log-goal-info))
    (if @stop-navstream
      (log/info "navstream-ended")
      (recur socket packet))))

(defn start-streaming-navdata [navdata-socket host port]
  (let [ receive-data (byte-array 2048)
        nav-datagram-receive-packet (new-datagram-packet receive-data host port)]
    (do
      (log/info "Starting navdata stream")
      (.setSoTimeout navdata-socket 1000)
      (future (stream-navdata navdata-socket
                              nav-datagram-receive-packet))
      (log/info "Creating navdata stream" ))))


(defn init-streaming-navdata [navdata-socket host port]
  (let [ send-data (byte-array (map byte [1 0 0 0]))
        nav-datagram-send-packet (new-datagram-packet send-data host port)
        receive-data (byte-array 2048)
        nav-datagram-receive-packet (new-datagram-packet receive-data host port)
        ]
    (do
      (reset-navstream)
      (.setSoTimeout navdata-socket 1000)
      (send-navdata navdata-socket nav-datagram-send-packet))))


(defn drone-init-navdata []
  (do
    (init-logger)
    (log/info "Initializing navdata")
    (reset! nav-data {})
    (init-streaming-navdata navdata-socket drone-host navdata-port)
    (drone :init-navdata)
    (drone :control-ack)
    (init-streaming-navdata navdata-socket drone-host navdata-port)
    (start-streaming-navdata navdata-socket drone-host navdata-port)
                                        ;(Thread/sleep 3000) ;Delay to let the logging catch up
    ))
