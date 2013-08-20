(ns clj-drone.example.video
  (:require [clj-drone.core :refer :all]
            [clj-drone.video :refer :all]))

;This video input from the drone is h264.  It is converted to png and
;displayed to the viewer

;The raw h264 output is save to a file vid.h264 for further conversion
;You can use ffmpeg -f h264 -an -i vid.h264 home.m4v to convert it
(drone-initialize)
(configure-save-video true)
(init-video (drone-ip :default))
(start-video (drone-ip :default))
(end-video)
