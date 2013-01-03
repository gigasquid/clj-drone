(ns clj-drone.example
  (:use clj-drone.core))

(initialize)
;Use ip and port for non-standard drone ip/port
;(initialize ip port)
(drone :take-off)
(drone :land)
(drone :emergency)
