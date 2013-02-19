(ns clj-drone.navdata
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:import (java.lang Float)))


(def nav-data (atom {}))
(def stop-navstream (atom false))
(def log-data (atom [:seq-num :pstate :com-watchdog :communication
                     :control-state :roll :pitch :yaw :altitude]))
(defn end-navstream [] (reset! stop-navstream true))
(defn reset-navstream [] (reset! stop-navstream false))
(defn set-log-data [data] (reset! log-data data))

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

(def detection-types
  {0 :horizontal-deprecated,
   1 :vertical-deprecated,
   2 :horizontal-drone-shell
   3 :none-disabled
   4 :roundel-under-drone
   5 :oriented-roundel-under-drone
   6 :oriented-roundel-front-drone
   7 :stripe-ground
   8 :roundel-front-drone
   9 :stripe
   10 :multiple
   11 :cap-orange-green-front-drone
   12 :black-white-roundel
   13 :2nd-verion-shell-tag-front-drone
   14 :tower-side-front-camera})

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

(defn get-int-by-n [ba offset n]
  (let [getf (fn [x y] (conj x (get-int ba (+ offset (* y 4)))))]
    (nth (reduce getf [] (range 0 (inc n))) n)))

(defn which-option-type [int]
  (case int
    0 :demo
    16 :target-detect
    :unknown))

(defn parse-target-tag [ba offset n]
  (let [noffset (+ offset 8)
        target-type (detection-types (get-int-by-n ba noffset n))
        target-xc (get-int-by-n ba (+ noffset 16) n)
        target-yc (get-int-by-n ba (+ noffset (* 2 16)) n)
        target-width (get-int-by-n ba (+ noffset (* 3 16)) n)
        target-height (get-int-by-n ba (+ noffset (* 4 16)) n)
        target-dist (get-int-by-n ba (+ noffset (* 5 16)) n )
        target-orient-angle (get-int-by-n ba (+ noffset (* 6 16)) n)
        taget-camera-source (get-int-by-n ba (+ noffset (* 6 16) 144 48) n)
        ]
    {:target-type target-type
     :target-xc target-xc :target-yc target-yc
     :target-width target-width :target-height target-height
     :target-dist target-dist :target-orient-angle target-orient-angle
     }
    ))


(defn parse-target-option [ba offset]
  (let [target-size 44
        target-num-tags-detected (get-int ba (+ offset 4))
        targets (for [i (range 0 target-num-tags-detected)]
                     (parse-target-tag ba (+ (+ offset 8) (* target-size i))))]
    {:targets-num target-num-tags-detected
     :targets (vec targets)}))

(defn parse-control-state [ba offset]
  (control-states (bit-shift-right (get-int ba offset) 16)))

(defn parse-demo-option [ba offset]
  (let [ control-state (parse-control-state ba (+ offset 4))
        battery (get-int ba (+ offset 8))
        pitch (float (/ (get-float ba (+ offset 12)) 1000))
        roll  (float (/ (get-float ba (+ offset 16)) 1000))
        yaw   (float (/ (get-float ba (+ offset 20)) 1000))
        altitude (float (/ (get-int ba (+ offset 24)) 1000))
        velocity-x (float (get-float ba (+ offset 28)))
        velocity-y (float (get-float ba (+ offset 32)))
        velocity-z (float (get-float ba (+ offset 26)))
        detect-camera-type (get-int ba (+ offset 96))
        ]
    { :control-state control-state
     :battery-percent battery
     :pitch pitch
     :roll roll
     :yaw yaw
     :altitude altitude
     :velocity-x velocity-x
     :velocity-y velocity-y
     :velocity-z velocity-z
     :detect-camera-type (detection-types detect-camera-type)
     }))

(defn parse-nav-state [state]
  (reduce
   #(let  [{:keys [name mask values]} %2
           bvalue (bit-and state (bit-shift-left 1 mask))]
      (conj %1 {name
                (if (= 0 bvalue) (first values) (last values))}))
   {}
   state-masks))


(defn parse-option [ba offset option-header]
  (case (which-option-type option-header)
      :demo (parse-demo-option ba offset)
      :target-detect (parse-target-option ba offset)
      nil))

(defn parse-options [ba offset options]
  (let [option-header (get-short ba offset)
        option-size (get-short ba (+ offset 2))
        option (when-not (zero? option-size) (parse-option ba offset option-header))
        next-offset (+ offset option-size)
        new-options (merge options option)]
    (if (or (zero? option-size) (>= next-offset (count ba)))
      new-options
      (parse-options ba next-offset new-options))))

(defn parse-navdata [navdata-bytes]
  (let [ header (get-int navdata-bytes 0)
        state (get-int navdata-bytes 4)
        seqnum (get-int navdata-bytes 8)
        vision-flag (= (get-int navdata-bytes 12) 1)
        pstate (parse-nav-state state)
        options (parse-options navdata-bytes 16 {})
        new-data (merge {:header header :seq-num seqnum :vision-flag vision-flag}
                        pstate options)]
    (swap! nav-data merge new-data)))

(defn send-navdata  [navdata-socket datagram-packet]
  (.send navdata-socket datagram-packet))

(defn receive-navdata  [navdata-socket datagram-packet]
  (.receive navdata-socket datagram-packet))

(defn get-navdata-bytes  [datagram-packet]
  (.getData datagram-packet))

(defn log-flight-data []
  (select-keys @nav-data @log-data))

