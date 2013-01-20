(ns clj-drone.navdata-test
  (:use clojure.test
        midje.sweet
    clj-drone.navdata)
  (:import (java.net InetAddress DatagramSocket)))

(def b-header [-120 119 102 85])
(def b-state [-48 4 -128 15])
(def header (map byte [-120 119 102 85]))
(def nav-input  (map byte (flatten (cons b-header b-state))))
(def host (InetAddress/getByName "192.168.1.1"))
(def port 5554)
(def socket (DatagramSocket. ))

(fact "about new-datagram-packet"
  (let [data (byte-array (map byte [1 0 0 0]))
         ndp (new-datagram-packet data host port)]
    (.getPort ndp) => port
    (.getAddress ndp) => host
    (.getData ndp) => data))

(fact "about get-int"
  (get-int (byte-array header) 0) => 0x55667788)

(fact "about parse-navdata"
  (parse-navdata nav-input) => anything
  @nav-data => (contains {:header 0x55667788})
  @nav-data => (contains {:battery :ok})
  @nav-data => (contains {:flying :landed})
  (against-background (before :facts (reset! nav-data {})))
  )


(fact "about init-streaming-navdata"
  (init-streaming-navdata socket host port) => anything
  (provided
    (send-navdata anything anything) => 1
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
