(ns clj-drone.at-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.at :refer :all]))

(deftest at-tests
  (fact "bit command vectors are translated into ints"
        (build-command-int [18 20 22 24 28]) => 290717696
        (build-command-int [9 18 20 22 24 28]) => 290718208)

  (fact "floats are cast to int"
        (int -1085485875) =>  (cast-float-to-int (float -0.8))
        (int -1085485875) => (cast-float-to-int (double -0.8)))

  (fact "commands are build correctly"
        (build-command :take-off 1) =>  "AT*REF=1,290718208\r"
        (build-command :land 2) => "AT*REF=2,290717696\r"
        (build-command :spin-right 3 0.5) => "AT*PCMD=3,1,0,0,0,1056964608\r"
        (build-command :spin-left 3 0.8) =>  "AT*PCMD=3,1,0,0,0,-1085485875\r"
        (build-command :up 3 0.5) =>  "AT*PCMD=3,1,0,0,1056964608,0\r"
        (build-command :down 3 0.8) =>  "AT*PCMD=3,1,0,0,-1085485875,0\r"
        (build-command :tilt-back 3 0.5) =>  "AT*PCMD=3,1,0,1056964608,0,0\r"
        (build-command :tilt-front 3 0.8) =>  "AT*PCMD=3,1,0,-1085485875,0,0\r"
        (build-command :tilt-right 3 0.5) =>  "AT*PCMD=3,1,1056964608,0,0,0\r"
        (build-command :tilt-left 3 0.8) =>  "AT*PCMD=3,1,-1085485875,0,0,0\r"
        (build-command :hover 3) =>  "AT*PCMD=3,0,0,0,0,0\r"
        (build-command :fly 3 0.5 -0.8 0.5 -0.8) => 
        "AT*PCMD=3,1,1056964608,-1085485875,1056964608,-1085485875\r"
        (build-command :fly 3 0 0 0 0.5) =>  (build-command :spin-right 3 0.5)
        (build-command :flat-trim 3) =>  "AT*FTRIM=3,\r"
        (build-command :reset-watchdog 3) => "AT*COMWDG=3,\r"
        (build-command :init-navdata 3) =>  "AT*CONFIG=3,\"general:navdata_demo\",\"FALSE\"\r"
        (build-command :control-ack 3) => "AT*CTRL=3,0\r"
        (build-command :init-targeting 3) =>  "AT*CONFIG=3,\"detect:detect_type\",\"12\"\r"
        ))

;; (run-tests 'clj-drone.at-test)