(ns clj-drone.at-test
  (:use clojure.test
        clj-drone.at))

(cast-float-to-int 0.5)
(int -1085485875)


(deftest building-command-int
  (is (= 290717696 (build-command-int [18 20 22 24 28])))
  (is (= 290718208 (build-command-int [9 18 20 22 24 28]))))

(deftest testing-cast-float-to-int
  (is (= (int -1085485875) (cast-float-to-int (float -0.8))))
  (is (= (int -1085485875) (cast-float-to-int (double -0.8)))))

(deftest building-commands
  (is (= (build-command :take-off 1) "AT*REF=1,290718208\r"))
  (is (= (build-command :land 2) "AT*REF=2,290717696\r"))
  (is (= (build-command :spin-right 3 0.5) "AT*PCMD=3,1,0,0,0,1056964608\r"))
  (is (= (build-command :spin-left 3 0.5) "AT*PCMD=3,1,0,0,0,-1056964608\r"))
  (is (= (build-command :up 3 0.5) "AT*PCMD=3,1,0,0,1056964608,0\r"))
  (is (= (build-command :down 3 0.5) "AT*PCMD=3,1,0,0,-1056964608,0\r"))
  (is (= (build-command :tilt-back 3 0.5) "AT*PCMD=3,1,0,1056964608,0,0\r"))
  (is (= (build-command :tilt-front 3 0.5) "AT*PCMD=3,1,0,-1056964608,0,0\r"))
  (is (= (build-command :tilt-right 3 0.5) "AT*PCMD=3,1,1056964608,0,0,0\r"))
  (is (= (build-command :tilt-left 3 0.5) "AT*PCMD=3,1,-1056964608,0,0,0\r"))
  )



(run-tests 'clj-drone.at-test)