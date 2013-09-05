(ns clj-drone.example.nav-goals
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))

;Logging goes to logs/drone.log

(set-log-data [:seq-num :battery-percent :control-state :detect-camera-type
               :targets-num :targets])

(def drone1-shakes (atom 0))
(def drone2-shakes (atom 0))

;;;;Drone 1

;;;Taking off

(def-belief-action ba-landed1
  "I (Drone1) am landed"
  (fn [{:keys [control-state]}] (= control-state :landed))
  (fn [navdata] (mdrone :drone1 :take-off)))

(def-belief-action ba-taking-off1
  "I (Drone1) am taking off"
  (fn [{:keys [control-state]}] (= control-state :trans-takeoff))
  (fn [navdata] (mdrone :drone1 :take-off)))

(def-goal g-take-off1
  "I (Drone1) want to fly."
  (fn [{:keys [control-state]}] (= control-state :hovering))
  [ba-landed1 ba-taking-off1])


;; Look for other drone and wave

(def-belief-action ba-not-see-other-drone1
  "I (Drone1) does not see the other drone"
  (fn [{:keys [targets-num]}] (= targets-num 0))
  (fn [navdata] (mdrone :drone1 :spin-right 0.1)))

(def-belief-action ba-see-the-other-drone1
  "I (Drone1)  see the other drone"
  (fn [{:keys [targets-num]}] (= targets-num 1))
  (fn [navdata] (do
                (mdrone :drone1 :anim-double-phi-theta-mixed)
                (Thread/sleep 5000)
                (swap! drone1-shakes inc)
)))


(def-goal g-find-other-drone-and-wave1
  "I (Drone1) want to find the other drone"
  (fn [_] (and (>= @drone1-shakes 1) (>= @drone2-shakes 1)))
  [ba-not-see-other-drone1 ba-see-the-other-drone1])

;;; Land

(def-belief-action ba-flying1
  "I (Drone1) am flying"
  (fn [{:keys [control-state]}] (or (= control-state :hovering) (= control-state :flying)))
  (fn [navdata] (do
                 (mdrone :drone1 :land))))

(def-belief-action ba-landing1
  "I (Drone1) am landing"
  (fn [{:keys [control-state]}] (= control-state :trans-landing))
  nil)

(def-goal g-land1
  "I (Drone1) want to land"
  (fn [{:keys [control-state]}] (= control-state :landed))
  [ba-flying1 ba-landing1])




;;;;Drone 2

;;;Taking off

(def-belief-action ba-landed2
  "I (Drone2) am landed"
  (fn [{:keys [control-state]}] (= control-state :landed))
  (fn [navdata] (mdrone :drone2 :take-off)))

(def-belief-action ba-taking-off2
  "I (Drone2) am taking off"
  (fn [{:keys [control-state]}] (= control-state :trans-takeoff))
  (fn [navdata] (mdrone :drone2 :take-off)))

(def-goal g-take-off2
  "I (Drone2) want to fly."
  (fn [{:keys [control-state]}] (= control-state :hovering))
  [ba-landed2 ba-taking-off2])



;; Look for other drone and wave

(def-belief-action ba-not-see-other-drone2
  "I (Drone2) does not see the other drone"
  (fn [{:keys [targets-num]}] (= targets-num 0))
  (fn [navdata] (mdrone :drone2 :spin-left 0.1)))

(def-belief-action ba-see-the-other-drone2
  "I (Drone2)  see the other drone"
  (fn [{:keys [targets-num]}] (= targets-num 1))
  (fn [navdata] (do
                 (mdrone :drone2 :anim-double-phi-theta-mixed)
                 (Thread/sleep 5000)
                 (swap! drone2-shakes inc))))


(def-goal g-find-other-drone-and-wave2
  "I (Drone2) want to find the other drone"
  (fn [_] (and (>= @drone1-shakes 1) (>= @drone2-shakes 1)))
  [ba-not-see-other-drone2 ba-see-the-other-drone2])




;;; Land

(def-belief-action ba-flying2
  "I (Drone2) am flying"
  (fn [{:keys [control-state]}] (or (= control-state :hovering) (= control-state :flying)))
  (fn [navdata] (do
                 (mdrone :drone2 :land))))

(def-belief-action ba-landing2
  "I (Drone2) am landing"
  (fn [{:keys [control-state]}] (= control-state :trans-landing))
  nil)

(def-goal g-land2
  "I (Drone2) want to land"
  (fn [{:keys [control-state]}] (= control-state :landed))
  [ba-flying2 ba-landing2])




;; ;;;  initialization to run

;; drone1 is neo green
;; drone2 is mine with blue

(do
  (reset! drone1-shakes 0)
  (reset! drone2-shakes 0)


  (drone-initialize :drone1  "192.168.1.100"  default-at-port)
  (mdrone :drone1 :init-targeting)
  (mdrone :drone1 :target-shell-h)
  (mdrone :drone1 :target-color-blue)
  (set-current-goal-list drones :drone1 [g-take-off1 g-find-other-drone-and-wave1 g-land1])
  (mdrone-init-navdata :drone1)
  (start-stream :drone1)



  (drone-initialize :drone2  "192.168.1.200"  default-at-port)
  (mdrone :drone2 :init-targeting)
  (mdrone :drone2 :target-shell-h)
  (mdrone :drone2 :target-color-green)
  (set-current-goal-list drones :drone2 [g-take-off2 g-find-other-drone-and-wave2 g-land2])
  (mdrone-init-navdata :drone2)
  (start-stream :drone2)


)

;; ;;If running in the repl end the nav-stream when done
(end-navstream)
