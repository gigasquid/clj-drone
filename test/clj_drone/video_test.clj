(ns clj-drone.video-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.video :refer :all]))

(def signature [80 97 86 69])

(defn make-bytes [v]
  (byte-array (map byte v)))

(fact "about read-signature"
  (read-signature (make-bytes signature)) => "PaVE")
