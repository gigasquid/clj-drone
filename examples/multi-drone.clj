(ns clj-drone.example.multidrone
  (:require [clj-drone.core :refer :all]))

(drone-initialize :drone1 "192.168.1.100" default-at-port default-navdata-port)
(mdrone :drone2 :take-off)
(mdrone :drone2 :land)

(drone-initialize :drone2 "192.168.1.200" default-at-port default-navdata-port)
(mdrone :drone1 :take-off)
(mdrone :drone1 :land)
