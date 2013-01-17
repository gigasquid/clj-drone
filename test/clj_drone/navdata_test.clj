(ns clj-drone.navdata-test
  (:use clojure.test
        midje.sweet
    clj-drone.navdata)
  (:import (java.net InetAddress DatagramSocket)))

(def header [(byte -120) (byte 119) (byte 102) (byte 85)])
(def host (InetAddress/getByName "192.168.1.1"))
(def port 5554)
(def socket (DatagramSocket. ))




(deftest navdata-tests

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
    (against-background (before :facts (reset! nav-data {})))))