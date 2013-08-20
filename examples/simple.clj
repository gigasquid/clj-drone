(ns clj-drone.example.simple
  (:require [clj-drone.core :refer :all]))

(drone-initialize)
;Use ip and port for non-standard drone ip/port
;(drone-initialize :default ip atport navdataport)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
