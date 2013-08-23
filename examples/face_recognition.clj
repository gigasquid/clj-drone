(ns clj-drone.example.video
  (:require [clj-drone.core :refer :all]
            [clj-drone.video :refer :all]
            [clj-drone.opencv :refer :all]))

;This video input from the drone is h264.  It is converted to png and
;then put through openvcv for face recognition
(configure-opencv true)
(drone-initialize)
(init-opencv)
(init-video (drone-ip :default))
(start-video (drone-ip :default))
(end-video)