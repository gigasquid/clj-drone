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


## License

Copyright © 2013 Carin Meier

Distributed under the Eclipse Public License, the same as Clojure.
