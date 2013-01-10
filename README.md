# clj-drone

Why wouldn't you want to control your AR Drone with your Clojure REPL?

A Clojure library to control the Parrot AR Drone 2.0 [http://ardrone2.parrot.com/](http://ardrone2.parrot.com/)

## Usage

Just beginning the start of the control library,so far only movement
commands are supported
More to come soon.

Using with Leiningen
```clojure
(defproject your-project "0.1.0-SNAPSHOT"
  :description "Your project"
  :dependencies [[clj-drone "0.1.0-SNAPSHOT"]])
````

Sample Usage - Drone Takes off for 10 seconds and then lands

```clojure
(ns clj-drone.example
  (:use clj-drone.core))

(drone-initialize)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
```

## Commands Supported

```clojure
(drone :take-off)
(drone :land)
(drone :emergency) ;; restores control of drone after emergency landing
(drone :hover)
(drone :flat-trim) ;; called on initialize as well to tell drone it is flat on the ground
```

The movement commands take argument from 0-1

```clojure
(drone :spin-right 0.5)
(drone :spin-left 1)
(drone :up 0.5)
(drone :down 1)
(drone :tilt-back 0.3)
(drone :tilt-front 1)
(drone :tilt-right 1)
(drone :tilt-left 1)
```

The fly command takes arguments left-right tilt, front-back tilt,
vertical speed, angular speed.  The arguments are all in the range -1
to 1.

```clojure
(drone :fly 0.3 -0.9 0.7 -0.2)
```

The drone-do-for command does a command for x second

```clojure
(drone-do-for 4 :take-off) ;=> take off for 4 seconds
(drone-do-for 2 :spin-right 0.8) ;=> spin right at 80% for 2 seconds
(drone-do-for 2 :spin-left 0.3) ;=> spin left at 30% for 2 seconds
```
The drone reset-watchdog command is used for when the drone command sequence
number gets out of sync.  It will ignore commands that come in with a bad or out of sync sequence number.  Calling this resets it to zero.  The drone will drop it's connection and reset its counter to 0 by itself if no command is received from the client for 2 seconds.
````clojure
(drone :reset-watchdog)
````

## To do list
- incoming navigation data stream
- incoming video stream

## License

Copyright © 2013 Carin Meier

Distributed under the Eclipse Public License, the same as Clojure.
