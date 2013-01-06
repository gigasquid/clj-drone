(ns clj-drone.example.moves
  (:use clj-drone.core))

(drone-initialize)
;Use ip and port for non-standard drone ip/port
;(initialize ip port)
(drone :take-off)
(drone :spin-right 1)
(drone :spin-left 1)
(drone :up 0.5)
(drone :down 1)
(drone :tilt-back 1)
(drone :tilt-front 1)
(drone :tilt-right 1)
(drone :tilt-left 1)
(drone :land)
