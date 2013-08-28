(ns clj-drone.navdata-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.core :refer :all])
  (:import (java.net InetAddress DatagramSocket)))

;; matrix 33 is 9 floats
;; vector 31 is 3 floats
(def b-matrix33  (vec (repeat (* 9 4) 0 )))
(def b-vector31  (vec (repeat (* 3 4) 0 )))
(* 12 4)

(def b-header [-120 119 102 85])
(def b-state [-48 4 -128 15])
(def b-seqnum [102 3 0 0])
(def b-vision [0 0 0 0])
(def b-demo-option-id [0 0])
(def b-demo-option-size [-108 0])
(def b-demo-control-state [0 0 2 0])
(def b-demo-battery [100 0 0 0])
(def b-demo-pitch [0 96 -122 -60])
(def b-demo-roll [0 -128 53 -59])
(def b-demo-yaw [0 0 87 -61])
(def b-demo-altitude [0 0 0 0])
(def b-demo-velocity-x [0 0 0 0])
(def b-demo-velocity-y [0 0 0 0])
(def b-demo-velocity-z [0 0 0 0])
(def b-demo-num-frames [0 0 0 0])
(def b-demo-detect-camera-rot b-matrix33)
(def b-demo-detect-camera-trans b-vector31)
(def b-demo-detect-tag-index [0 0 0 0])
(def b-demo-detect-camera-type [4 0 0 0])
(def b-demo-drone-camera-rot b-matrix33)
(def b-demo-drone-camera-trans b-vector31)
(def b-demo-option (flatten (conj b-demo-option-id b-demo-option-size
                                  b-demo-control-state b-demo-battery
                                  b-demo-pitch b-demo-roll b-demo-yaw
                                  b-demo-altitude b-demo-velocity-x
                                  b-demo-velocity-y b-demo-velocity-z
                                  b-demo-num-frames
                                  b-demo-detect-camera-rot b-demo-detect-camera-trans
                                  b-demo-detect-tag-index
                                  b-demo-detect-camera-type b-demo-drone-camera-rot
                                  b-demo-drone-camera-trans)))
(def b-target-option-id [16 0])
(def b-target-option-size [72 1])
(def b-target-num-tags-detected [2 0 0 0])
(def b-target-type [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-xc [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-yc [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-width [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-height [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-dist [1 0 0 0 2 0 0 0 3 0 0 0 4 0 0 0])
(def b-target-orient-angle [0 96 -122 -60 0 96 -122 -60 0 96 -122 -60 0 96 -122 -60])
(def b-target-rotation (flatten (conj b-matrix33  b-matrix33  b-matrix33  b-matrix33)))
(def b-target-translation (flatten (conj b-vector31 b-vector31 b-vector31 b-vector31)))
(def b-target-camera-source [1 0 0 0 2 0 0 0 2 0 0 0 2 0 0 0])
(def b-target-option (flatten (conj b-target-option-id b-target-option-size
                                    b-target-num-tags-detected
                                    b-target-type b-target-xc b-target-yc
                                    b-target-width b-target-height b-target-dist
                                    b-target-orient-angle b-target-rotation b-target-translation
                                    b-target-camera-source)))

(def b-options (flatten (conj b-demo-option b-target-option)))
(def header (map byte [-120 119 102 85]))
(def nav-input  (map byte (flatten (conj b-header b-state b-seqnum b-vision b-demo-option b-target-option))))
(def host (InetAddress/getByName "192.168.1.1"))
(def port 5554)
(def socket (DatagramSocket. ))
(def packet (new-datagram-packet (byte-array 2048) host port))

(fact "about new-datagram-packet"
      (let [data (byte-array (map byte [1 0 0 0]))
            ndp (new-datagram-packet data host port)]
        (.getPort ndp) => port
        (.getAddress ndp) => host
        (.getData ndp) => data))

(fact "about get-int"
      (get-int (byte-array header) 0) => 0x55667788)

(fact "about get-short"
      (get-short (map byte b-demo-option-size) 0) => 148)

(fact "about get-float"
      (get-float (map byte b-demo-pitch) 0) => -1075.0)

(facts "about get-int-by-n"
       (get-int-by-n (map byte b-target-type) 0 0) => 1
       (get-int-by-n (map byte b-target-type) 0 1) => 2
       (get-int-by-n (map byte b-target-type) 0 2) => 3
       (get-int-by-n (map byte b-target-type) 0 3) => 4)


(facts "about get-float-by-n"
       (get-float-by-n (map byte b-demo-pitch) 0 0) => -1075.0)

(fact "about parse-control-state"
      (parse-control-state b-demo-option 4) => :landed)

(fact "about parse-demo-option"
      (let [option (parse-demo-option b-demo-option 0)]
        option => (contains {:control-state :landed})
        option => (contains {:battery-percent 100 })
        option => (contains {:pitch (float -1.075) })
        option => (contains {:roll (float -2.904) })
        option => (contains {:yaw (float -0.215) })
        option => (contains {:altitude  (float 0.0) })
        option => (contains {:velocity-x  (float 0.0) })
        option => (contains {:velocity-y  (float 0.0) })
        option => (contains {:velocity-z  (float 0.0) })
        option => (contains {:detect-camera-type :roundel-under-drone })
        ))


(fact "about parse-navdata"
      (parse-navdata nav-input (get-nav-data :default)) => anything
      @(get-nav-data :default) => (contains {:header 0x55667788})
      @(get-nav-data :default) => (contains {:battery :ok})
      @(get-nav-data :default) => (contains {:flying :landed})
      @(get-nav-data :default) => (contains {:seq-num 870})
      @(get-nav-data :default) => (contains {:vision-flag false})
      @(get-nav-data :default) => (contains {:control-state :landed})
      @(get-nav-data :default) => (contains {:battery-percent 100 })
      @(get-nav-data :default) => (contains {:pitch (float -1.075) })
      @(get-nav-data :default) => (contains {:roll (float -2.904) })
      @(get-nav-data :default) => (contains {:yaw (float -0.215) })
      @(get-nav-data :default) => (contains {:altitude (float 0.0) })
      @(get-nav-data :default) => (contains {:velocity-x (float 0.0)})
      @(get-nav-data :default) => (contains {:velocity-y (float 0.0)})
      @(get-nav-data :default) => (contains {:velocity-z (float 0.0)})
      (against-background (before :facts (reset! drones {:default {:nav-data (atom {})}}))))


(fact "about stream-navdata"
      (stream-navdata nil socket packet) => anything
      (provided
        (receive-navdata anything anything) => 1
        (get-nav-data :default) => (:nav-data (:default @drones))
        (get-navdata-bytes anything) => nav-input)
      (against-background
        (before :facts (do
                         (reset! drones {:default {:nav-data (atom {})
                                                   :host (InetAddress/getByName "192.168.1.1")}})
                         (reset! stop-navstream true)))))


(fact "about parse-nav-state"
      (let [ state 260048080
            result (parse-nav-state state)
            {:keys [ flying video vision control altitude-control
                    user-feedback command-ack camera travelling
                    usb demo bootstrap motors communication
                    software battery emergency-landing timer
                    magneto angles wind ultrasound cutout
                    pic-version atcodec-thread navdata-thread
                    video-thread acquisition-thread ctrl-watchdog
                    adc-watchdog com-watchdog emergency]} result]
        flying => :landed
        video => :off
        vision => :off
        control => :euler-angles
        altitude-control => :on
        user-feedback => :off
        command-ack => :received
        camera => :ready
        travelling => :off
        usb => :not-ready
        demo => :on
        bootstrap => :off
        motors => :ok
        communication => :ok
        software => :ok
        battery => :ok
        emergency-landing => :off
        timer => :not-elapsed
        magneto => :ok
        angles => :ok
        wind => :ok
        ultrasound => :ok
        cutout => :ok
        pic-version => :ok
        atcodec-thread => :on
        navdata-thread => :on
        video-thread => :on
        acquisition-thread => :on
        ctrl-watchdog => :ok
        adc-watchdog => :ok
        com-watchdog => :ok
        emergency => :ok
        ))

(fact  "about which-option-type"
      (which-option-type 0) => :demo
      (which-option-type 16) => :target-detect
      (which-option-type 2342342) => :unknown)

(fact "about parse-tag-detect"
      (parse-tag-detect 131072) => :vertical-hsync)

(fact "about parse-target-tag with the first target"
      (let [tag (parse-target-tag (map byte b-target-option) 0 0)]
        tag => (contains {:target-type :horizontal})
        tag => (contains {:target-xc 1})
        tag => (contains {:target-yc 1})
        tag => (contains {:target-width 1})
        tag => (contains {:target-height 1})
        tag => (contains {:target-dist 1})
        tag => (contains {:target-orient-angle -1075.0})
        tag => (contains {:target-camera-source :vertical})))

(fact "about parse-target-tag with the second target"
      (let [tag (parse-target-tag (map byte b-target-option) 0 1)]
        tag => (contains {:target-type :horizontal})
        tag => (contains {:target-xc 2})
        tag => (contains {:target-yc 2})
        tag => (contains {:target-width 2})
        tag => (contains {:target-height 2})
        tag => (contains {:target-dist 2})
        tag => (contains {:target-orient-angle -1075.0})
        tag => (contains {:target-camera-source :vertical-hsync})))

(fact "about parse-target-tag with the third target"
      (let [tag (parse-target-tag (map byte b-target-option) 0 2)]
        tag => (contains {:target-type :horizontal})
        tag => (contains {:target-xc 3})
        tag => (contains {:target-yc 3})
        tag => (contains {:target-width 3})
        tag => (contains {:target-height 3})
        tag => (contains {:target-dist 3})
        tag => (contains {:target-orient-angle -1075.0})
        tag => (contains {:target-camera-source :vertical-hsync})))

(fact "about parse-target-tag with the fourth target"
      (let [tag (parse-target-tag (map byte b-target-option) 0 3)]
        tag => (contains {:target-type :horizontal})
        tag => (contains {:target-xc 4})
        tag => (contains {:target-yc 4})
        tag => (contains {:target-width 4})
        tag => (contains {:target-height 4})
        tag => (contains {:target-dist 4})
        tag => (contains {:target-orient-angle -1075.0})
        tag => (contains {:target-camera-source :vertical-hsync})))


(fact "about parse-target-option"
      (let [t-tag {:target-type :horizontal
                   :target-xc 1
                   :target-yc 1
                   :target-width 1
                   :target-height 1
                   :target-dist 1
                   :target-orient-angle -1075.0
                   :target-camera-source 1}
            option (parse-target-option b-target-option 0)
            targets (:targets option)]
        option => (contains {:targets-num 2})
        (count targets) => 2
        (first targets) => (contains {:target-type :horizontal})))

(fact "about parse option with demo"
      (let [option (parse-options b-demo-option 0 {})]
        option => (contains {:control-state :landed})))

(fact "about parse option with targets"
      (let [option (parse-options b-target-option 0 {})]
        option => (contains {:targets-num 2})))

(fact "about parse-options with demo and targets"
      (let [options (parse-options nav-input 16 {})]
        options => (contains {:control-state :landed})
        options => (contains {:targets-num 2})))


