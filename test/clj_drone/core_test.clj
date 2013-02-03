(ns clj-drone.core-test
  (:import (java.net DatagramPacket))
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.core :refer :all]
            [clj-drone.at :refer :all]))

(deftest core-tests
  (fact "default initialize gets default host and port"
        (.getHostName drone-host) => default-drone-ip
        at-port => default-at-port
        navdata-port => default-navdata-port
        @counter => 1
        (against-background (before :facts (drone-initialize))))

  (fact "custom initiliaze uses custom host and port"
        (.getHostName drone-host) => "192.168.2.2"
        at-port => 4444
        navdata-port => 3333
        @counter => 1
        (against-background (before :facts (drone-initialize "192.168.2.2" 4444 3333))))

  (fact "drone command passes along the data to send-command"
        (drone :take-off) => anything
        (provided
         (send-command "AT*REF=2,290718208\r") => 1)
        (against-background (before :facts (drone-initialize))))

  (fact "drone-do-for command calls drone command every 30 sec"
        (drone-do-for 1 :take-off) => anything
        (provided
         (drone :take-off nil nil nil nil) => 1 :times #(< 0 %1))
        (against-background (before :facts (drone-initialize)))))


;; (run-tests 'clj-drone.core-test)