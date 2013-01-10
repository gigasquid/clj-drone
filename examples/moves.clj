(ns clj-drone.example.moves
  (:use clj-drone.core))

(drone-initialize)
;Use ip and port for non-standard drone ip/port
;(initialize ip port)
(drone-do-for 4 :take-off)
(drone-do-for 2 :up 0.3)
(drone-do-for 3.75 :fly 0.2 0 0 0.5) ; sprial
(drone :hover)
(drone :land)