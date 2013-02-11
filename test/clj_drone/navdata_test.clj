(ns clj-drone.navdata-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.core :refer :all])
  (:import (java.net InetAddress DatagramSocket)))

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
(def b-demo-extra (vec (repeat 108 0 )))
(def b-demo-option (flatten (conj b-demo-option-id b-demo-option-size
                                  b-demo-control-state b-demo-battery
                                  b-demo-pitch b-demo-roll b-demo-yaw
                                  b-demo-altitude b-demo-velocity-x
                                  b-demo-velocity-y b-demo-velocity-z
                                  b-demo-extra)))
(def b-target-option-id [16 0])
(def b-target-option-size [72 1])
(def b-target-num-tags-detected [2 0 0 0])
(def b-target-type [0 0 0 0])
(def b-target-xc [0 0 0 0])
(def b-target-yc [0 0 0 0])
(def b-target-width [0 0 0 0])
(def b-target-height [0 0 0 0])
(def b-target-dist [0 0 0 0])
(def b-target-orient-angle [0 0 0 0])
(def b-target-rotation [0 0 0 0])
(def b-target-translation [0 0 0 0])
(def b-target-camera-source [0 0 0 0])
(def b-target-tag (flatten (conj b-target-type b-target-xc b-target-yc
                                 b-target-width b-target-height b-target-dist
                                 b-target-orient-angle b-target-rotation b-target-translation
                                 b-target-camera-source)))
(def b-target-option (flatten (conj b-target-option-id b-target-option-size
                                    b-target-num-tags-detected
                                    b-target-tag b-target-tag)))
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

(fact "acout get-float"
      (get-float (map byte b-demo-pitch) 0) => -1075.0)

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
        ))

(fact "about parse-navdata"
      (parse-navdata nav-input) => anything
      @nav-data => (contains {:header 0x55667788})
      @nav-data => (contains {:battery :ok})
      @nav-data => (contains {:flying :landed})
      @nav-data => (contains {:seq-num 870})
      @nav-data => (contains {:vision-flag false})
      @nav-data => (contains {:control-state :landed})
      @nav-data => (contains {:battery-percent 100 })
      @nav-data => (contains {:pitch (float -1.075) })
      @nav-data => (contains {:roll (float -2.904) })
      @nav-data => (contains {:yaw (float -0.215) })
      @nav-data => (contains {:altitude (float 0.0) })
      @nav-data => (contains {:velocity-x (float 0.0)})
      @nav-data => (contains {:velocity-y (float 0.0)})
      @nav-data => (contains {:velocity-z (float 0.0)})
      (against-background (before :facts (reset! nav-data {}))))


(fact "about stream-navdata"
      (stream-navdata socket packet) => anything
      (provided
       (receive-navdata anything anything) => 1
       (get-navdata-bytes anything) => nav-input)
      (against-background (before :facts (reset! stop-navstream true))))


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

(fact "about parse-target-tag"
      (let [tag (parse-target-tag b-target-tag 0)]
        tag => (contains {:target-type :horizontal-deprecated})
        tag => (contains {:target-xc 0})
        tag => (contains {:target-yc 0})
        tag => (contains {:target-width 0})
        tag => (contains {:target-height 0})
        tag => (contains {:target-dist 0})
        tag => (contains {:target-orient-angle 0.0})))

(fact "about parse-target-option"
      (let [t-tag {:target-type :horizontal-deprecated
                   :target-xc 0
                   :target-yc 0
                   :target-width 0
                   :target-height 0
                   :target-dist 0
                   :target-orient-angle 0.0}
            option (parse-target-option b-target-option 0)
            targets (:targets option)]
        option => (contains {:targets-num 2})
        (count targets) => 2
        (first targets) => (contains {:target-type :horizontal-deprecated})))

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
