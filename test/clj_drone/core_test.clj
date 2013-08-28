(ns clj-drone.core-test
  (:import (java.net DatagramPacket InetAddress))
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.core :refer :all]))

(deftest core-tests
  (facts "default initialize gets default host and port"
    (.getHostName (:host (:default @drones))) => default-drone-ip
    (:at-port (:default @drones)) => default-at-port
    @(:counter (:default @drones)) => 1
    (against-background (before :facts (drone-initialize))))

  (fact "custom initiliaze uses custom name host and port"
    (.getHostName (:host (:frank @drones))) => "192.168.2.2"
    (:at-port (:frank @drones)) => 4444
    @(:counter (:frank @drones)) => 1
    (against-background (before :facts (drone-initialize :frank "192.168.2.2" 4444))))

  (fact "drone command passes along the data to send-command"
    (drone :take-off) => anything
    (provided
      (send-command :default "AT*REF=2,290718208\r") => 1)
    (against-background (before :facts (drone-initialize))))

  (fact "drone-do-for command calls drone command every 30 sec"
    (drone-do-for 1 :take-off) => anything
    (provided
      (mdrone :default :take-off nil nil nil nil) => 1 :times #(< 0 %1))
    (against-background (before :facts (drone-initialize))))

  (fact "find-drone finds the drone by ip"
    (find-drone "192.168.1.2") => {:drone2 {:host (InetAddress/getByName"192.168.1.2")}}
    (against-background
      (before :facts
              (reset! drones {:drone1 {:host
                                       (InetAddress/getByName "192.168.1.1")}
                              :drone2 {:host
                                       (InetAddress/getByName"192.168.1.2")}})))))



;; (run-tests 'clj-drone.core-test)