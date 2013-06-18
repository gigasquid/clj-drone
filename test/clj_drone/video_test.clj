(ns clj-drone.video-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.video :refer :all]))

(def tdata (read-string (slurp "test/data/video-frame.data")))
(def testvideo (map byte tdata))

(def signature [80 97 86 69])

(defn make-bytes [v]
  (byte-array (map byte v)))

(fact "about read-signature"
  (read-signature (make-bytes signature)) => "PaVE"
  (read-signature testvideo) => "PaVE")

(fact "about get-header"
  (get-header testvideo) => (contains {:version 2})
  (get-header testvideo) => (contains {:codec 4})
  (get-header testvideo) => (contains {:header-size 68})
  (get-header testvideo) => (contains {:payload-size 19125})
  (get-header testvideo) => (contains {:encoded-width 640})
  (get-header testvideo) => (contains {:encoded-height 368})
  (get-header testvideo) => (contains {:display-width 640})
  (get-header testvideo) => (contains {:display-height 360})
  (get-header testvideo) => (contains {:frame-number 1866}))

(fact "about header-size"
  header-size => 68)

(fact "about payload-size"
  (payload-size testvideo) => 19125)
