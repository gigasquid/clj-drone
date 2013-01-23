(ns clj-drone.navdata
  (require [ clj-logging-config.log4j :as log-config]
           [ clojure.tools.logging :as log])
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:import (java.lang Float)))

(log-config/set-logger! :level :debug
                        :out (org.apache.log4j.FileAppender.
                              (org.apache.log4j.EnhancedPatternLayout. org.apache.log4j.EnhancedPatternLayout/TTCC_CONVERSION_PATTERN)
                              "logs/drone.log"
                               true))


(def nav-data (atom {}))
(def stop-navstream (atom false))

(def state-masks
  [ {:name :flying             :mask 0  :values [:landed :flying]}
    {:name :video              :mask 1  :values [:off :on]}
    {:name :vision             :mask 2  :values [:off :on]}
    {:name :control            :mask 3  :values [:euler-angles :angular-speed]}
    {:name :altitude-control   :mask 4  :values [:off :on]}
    {:name :user-feedback      :mask 5  :values [:off :on]}
    {:name :command-ack        :mask 6  :values [:none :received]}
    {:name :camera             :mask 7  :values [:not-ready :ready]}
    {:name :travelling         :mask 8  :values [:off :on]}
    {:name :usb                :mask 9  :values [:not-ready :ready]}
    {:name :demo               :mask 10 :values [:off :on]}
    {:name :bootstrap          :mask 11 :values [:off :on]}
    {:name :motors             :mask 12 :values [:ok :motor-problem]}
    {:name :communication      :mask 13 :values [:ok :communication-lost]}
    {:name :software           :mask 14 :values [:ok :software-fault]}
    {:name :battery            :mask 15 :values [:ok :too-low]}
    {:name :emergency-landing  :mask 16 :values [:off :on]}
    {:name :timer              :mask 17 :values [:not-elapsed :elapsed]}
    {:name :magneto            :mask 18 :values [:ok :needs-calibration]}
    {:name :angles             :mask 19 :values [:ok :out-of-range]}
    {:name :wind               :mask 20 :values [:ok :too-much]}
    {:name :ultrasound         :mask 21 :values [:ok :deaf]}
    {:name :cutout             :mask 22 :values [:ok :detected]}
    {:name :pic-version        :mask 23 :values [:bad-version :ok]}
    {:name :atcodec-thread     :mask 24 :values [:off :on]}
    {:name :navdata-thread     :mask 25 :values [:off :on]}
    {:name :video-thread       :mask 26 :values [:off :on]}
    {:name :acquisition-thread :mask 27 :values [:off :on]}
    {:name :ctrl-watchdog      :mask 28 :values [:ok :delay]}
    {:name :adc-watchdog       :mask 29 :values [:ok :delay]}
    {:name :com-watchdog       :mask 30 :values [:ok :problem]}
    {:name :emergency          :mask 31 :values [:ok :detected]}
    ])

(def control-states
  {0 :default, 1 :init, 2 :landed, 3 :flying, 4 :hovering, 5 :test,
   6 :trans-takeoff, 7 :trans-gotofix, 8 :trans-landing, 9 :trans-looping})

(def option-tags [0 :NAVDATA-DEMO-TAG])

(defn new-datagram-packet [data host port]
  (new DatagramPacket data (count data) host port))

(defn bytes-to-int [ba offset num-bytes]
  (let [c 0x000000FF]
    (reduce
      #(+ %1 (bit-shift-left (bit-and (nth ba (+ offset %2)) c) (* 8 %2)))
      0
      (range num-bytes))))

(defn get-int [ba offset]
  (bytes-to-int ba offset 4))

(defn get-short [ba offset]
  (bytes-to-int ba offset 2))

(defn get-float [ba offset]
  (Float/intBitsToFloat (Integer. (bytes-to-int ba offset 4))))

(defn parse-control-state [ba offset]
  (control-states (bit-shift-right (get-int ba offset) 16)))

(defn parse-demo-option [ba offset]
  (let [ control-state (parse-control-state ba (+ offset 4))
         battery (get-int ba (+ offset 8))
         pitch (/ (get-float ba (+ offset 12)) 1000)
         roll  (/ (get-float ba (+ offset 16)) 1000)
         yaw   (/ (get-float ba (+ offset 20)) 1000)
         altitude (/ (get-int ba (+ offset 24)) 100)
         ]
    { :control-state control-state
      :battery-percent battery
      :pitch pitch
      :roll roll
      :yaw yaw
      :altitude altitude}))

(defn parse-nav-state [state]
  (reduce
    #(let  [{:keys [name mask values]} %2
             bvalue (bit-and state (bit-shift-left 1 mask))]
       (conj %1 {name
                  (if (= 0 bvalue) (first values) (last values))}))
    {}
    state-masks))

(defn parse-navdata [navdata-bytes]
  (let [ header (get-int navdata-bytes 0)
         state (get-int navdata-bytes 4)
         seqnum (get-int navdata-bytes 8)
         vision-flag (= (get-int navdata-bytes 12) 1)
         pstate (parse-nav-state state)
         demo-option (parse-demo-option navdata-bytes 16)
         new-data (merge {:header header :seq-num seqnum :vision-flag vision-flag}
                          pstate demo-option)]
    (swap! nav-data merge new-data)))

(defn send-navdata  [navdata-socket datagram-packet]
  (.send navdata-socket datagram-packet))

(defn receive-navdata  [navdata-socket datagram-packet]
  (.receive navdata-socket datagram-packet))

(defn get-navdata-bytes  [datagram-packet]
  (.getData datagram-packet))

(defn stream-navdata [socket packet]
  (do
    (receive-navdata socket packet)
    (parse-navdata (get-navdata-bytes packet))
    (log/info (str "Navdata: " @nav-data))
    (if @stop-navstream
      "navstream ended"
      (recur socket packet))))

(defn init-streaming-navdata [navdata-socket host port]
  (let [ send-data (byte-array (map byte [1 0 0 0]))
         nav-datagram-send-packet (new-datagram-packet send-data host port)
         receive-data (byte-array 2048)
         nav-datagram-receive-packet (new-datagram-packet receive-data host port)
         ]
    (do
      (.setSoTimeout navdata-socket 1000)
      (send-navdata navdata-socket nav-datagram-send-packet)
      (future (stream-navdata navdata-socket nav-datagram-receive-packet))
      )))

