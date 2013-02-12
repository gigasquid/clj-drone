(ns clj-drone.example.nav-test
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;; logging is configured to go to the logs/drone.log file

(set-log-data [:seq-num :battery-percent :control-state :detect-camera-type
               :targets-num :targets])
(drone-initialize)
(drone :init-targeting)
(drone :target-shell-h)
(drone :target-roundel-v)
(drone-init-navdata)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
(end-navstream)
@nav-data

(bit-shift-left 1 5)

(bit-and 1 (bit-shift-left 1 2))
