(ns clj-drone.example.moves
  (:use clj-drone.core))

(drone-initialize)
;Use ip and port for non-standard drone ip/port
;(initialize ip port)
(drone-do-for 4 :take-off)
(drone-do-for 2 :up 0.3)
(drone-do-for 2 :tilt-right 0.5)
(drone-do-for 2 :spin-right 0.8)
(drone-do-for 2 :spin-left 0.5)
(drone :hover)
(drone :land)
