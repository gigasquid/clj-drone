(ns clj-drone.core-test
  (:use clojure.test
        clj-drone.core))

(deftest testing-drone-initialize
  (testing "defaults"
    (drone-initialize)
    (is (= (.getHostName drone-host) default-drone-ip))
    (is (= at-port default-at-port)))

  (testing "custom"
    (drone-initialize "192.168.2.2" 4444)
    (is (= (.getHostName drone-host) "192.168.2.2"))
    (is (= at-port 4444))))

;; (run-tests 'clj-drone.core-test)