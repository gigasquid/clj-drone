(ns clj-drone.navdata
  (:import (java.net DatagramPacket DatagramSocket InetAddress)))

(def nav-data (atom {}))

(defn new-datagram-packet [data host port]
  (new DatagramPacket data (count data) host port))

(defn get-int [ba offset]
  (let [c 0x000000FF]
    (reduce
      #(+ %1 (bit-shift-left (bit-and (nth ba (+ offset %2)) c) (* 8 %2)))
      0
      [0 1 2 3])))

(defn parse-navdata [navdata-bytes]
  (let [header (get-int navdata-bytes 0)]
    (swap! nav-data assoc :header header)))

(defn send-navdata  [navdata-socket datagram-packet]
  (.send navdata-socket datagram-packet))

(defn receive-navdata  [navdata-socket datagram-packet]
  (.receive navdata-socket datagram-packet))

(defn get-navdata-bytes  [datagram-packet]
  (.getData datagram-packet))

(defn init-streaming-navdata [navdata-socket host port]
  (let [ send-data (byte-array (map byte [1 0 0 0]))
         nav-datagram-send-packet (new-datagram-packet send-data host port)
         receive-data (byte-array 4096)
         nav-datagram-receive-packet (new-datagram-packet receive-data host port)
         ]
    (do
      (.setSoTimeout navdata-socket 1000)
      (send-navdata navdata-socket nav-datagram-send-packet)
      (receive-navdata navdata-socket nav-datagram-receive-packet)
      (parse-navdata (get-navdata-bytes nav-datagram-receive-packet))
      )))

