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

