(ns clj-drone.example.simple
  (:use clj-drone.core))

(drone-initialize)
;Use ip and port for non-standard drone ip/port
;(initialize ip port)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
