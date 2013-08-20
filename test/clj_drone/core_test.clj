(ns clj-drone.core-test
  (:import (java.net DatagramPacket))
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.core :refer :all]))

(deftest core-tests
  (facts "default initialize gets default host and port"
        (.getHostName (:host (:default @drones))) => default-drone-ip
        (:at-port (:default @drones)) => default-at-port
        (:navdata-port (:default @drones)) => default-navdata-port
        @(:counter (:default @drones)) => 1
        (against-background (before :facts (drone-initialize))))

  (fact "custom initiliaze uses custom name host and port"
        (.getHostName (:host (:frank @drones))) => "192.168.2.2"
        (:at-port (:frank @drones)) => 4444
        (:navdata-port (:frank @drones)) => 3333
        @(:counter (:frank @drones)) => 1
        (against-background (before :facts (drone-initialize :frank "192.168.2.2" 4444 3333))))

  (fact "drone command passes along the data to send-command"
        (drone :take-off) => anything
        (provided
         (send-command :default "AT*REF=2,290718208\r") => 1)
        (against-background (before :facts (drone-initialize))))

  (fact "drone-do-for command calls drone command every 30 sec"
        (drone-do-for 1 :take-off) => anything
        (provided
         (mdrone :default :take-off nil nil nil nil) => 1 :times #(< 0 %1))
        (against-background (before :facts (drone-initialize)))))


;; (run-tests 'clj-drone.core-test)