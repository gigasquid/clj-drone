(ns clj-drone.at-test
  (:use clojure.test
        clj-drone.at))

(deftest building-command-int
  (is (= 290717696 (build-command-int [18 20 22 24 28])))
  (is (= 290718208 (build-command-int [9 18 20 22 24 28]))))

(deftest building-commands
  (is (= (build-command :take-off 1) "AT*REF=1,290718208\r"))
  (is (= (build-command :land 2) "AT*REF=2,290717696\r")))

;(run-tests 'clj-drone.at-test)