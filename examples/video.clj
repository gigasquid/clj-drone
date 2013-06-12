(ns clj-drone.example.video
  (:require [clj-drone.core :refer :all]
            [clj-drone.video :refer :all]))

;This records raw video to a file called stream.m4v
;To convert to video use
;ffmpeg -f h264 -an -i vid.h264 stream.m4v


(drone-initialize)
(drone-init-video)
(start-video "192.168.1.1")
(drone :flat-trim)
(drone :take-off)
(drone :land)
(drone-end-video)
