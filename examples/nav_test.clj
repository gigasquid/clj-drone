(ns clj-drone.example.nav-test
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;; logging is configured to go to the logs/drone.log file

(set-log-data [:seq-num :flying :battery-percent :control-state :roll :pitch :yaw
                :velocity-x :velocity-y :velocity-z])
(drone-initialize)

(drone-init-navdata)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
(end-navstream)

(get-nav-data :default)
(:nav-agent (:default @drones))
(.getHostAddress (:host (:default @drones)))

(let [m {:a 1 :b 2 :c 1}]
  (select-keys m (for [[k v] m :when (= v 1)] k)))

(defn find-drone [ip]
  (select-keys @drones (for [[k v] @drones :when (= ip (.getHostAddress (:host v)))] k)))

(find-drone "192.168.1.1")

(filter (fn [x] (= "192.168.1.1" (.getHostAddress (:host x)))) @drones)

(agent-errors (:nav-agent (:default @drones)))

