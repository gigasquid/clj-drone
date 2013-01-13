(ns clj-drone.core-test
  (:use clojure.test
        midje.sweet
        clj-drone.core))

(deftest core-tests
  (fact "default initialize gets default host and port"
    (.getHostName drone-host) => default-drone-ip
    at-port => default-at-port
    (against-background (before :facts (drone-initialize))))

  (fact "custom initiliaze uses custom host and port"
    (.getHostName drone-host) => "192.168.2.2"
    at-port => 4444
    (against-background (before :facts (drone-initialize "192.168.2.2" 4444)))))

;; (run-tests 'clj-drone.core-test)