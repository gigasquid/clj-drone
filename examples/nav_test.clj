(ns clj-drone.example.nav-test
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;; logging is configured to go to the logs/drone.log file

(set-log-data [:seq-num :battery-percent :control-state :detect-camera-type
               :targets-num :targets])
(drone-initialize)
(drone :init-targeting)
(drone :target-shell-h)
(drone :target-color-blue)
;; the drone will look for targets with blue tags on the horizontal camera
(drone :target-roundel-v)
;; the drone will look for the black and white roundel on the vertical camera
(drone-init-navdata)
;; watch the drone.log file and move the drone above the roundel
;; target and put the hull in front of the horizontal camera

(end-navstream)  ;; this ends the logging
