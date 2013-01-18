(ns clj-drone.navdata-test
  (:use clojure.test
        midje.sweet
    clj-drone.navdata)
  (:import (java.net InetAddress DatagramSocket)))

(def header [(byte -120) (byte 119) (byte 102) (byte 85)])
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
  (parse-navdata header) => anything
  @nav-data => {:header 0x55667788}
  (against-background (before :facts (reset! nav-data {}))))


(fact "about init-streaming-navdata"
  (init-streaming-navdata socket host port) => {:header 0x55667788}
  (provided
    (send-navdata anything anything) => 1
    (receive-navdata anything anything) => 1
    (get-navdata-bytes anything) => header))
