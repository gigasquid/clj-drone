# clj-drone

Why wouldn't you want to control your AR Drone with your Clojure REPL?

A Clojure library to control the Parrot AR Drone 2.0 [http://ardrone2.parrot.com/](http://ardrone2.parrot.com/)

## Usage

Just beginning the start of the control library, so far only take-off,
land and emergency landing are implemented.

More to come soon.

Sample Usage - Drone Takes off for 10 seconds and then lands

    (ns clj-drone.example
      (:use clj-drone.core))

    (drone-initialize)
    (drone :take-off)
    (Thread/sleep 10000)
    (drone :land)

## Commands Supported

    (drone :take-off)
    (drone :land)
    (drone :emergency)
    ;;movement commands take argument from 0-1
    (drone :spin-right 0.5)
    (drone :spin-left 1)
    (drone :up 0.5)
    (drone :down 1)
    (drone :tilt-back 0.3)
    (drone :tilt-front 1)
    (drone :tilt-right 1)
    (drone :tilt-left 1)


## License

Copyright © 2013 Carin Meier

Distributed under the Eclipse Public License, the same as Clojure.
