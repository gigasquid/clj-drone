(ns clj-drone.example.nav-goals
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))

;Logging goes to logs/drone.log

(set-log-data [:seq-num :control-state :altitude])

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


;; Cruising Altitude

(def-belief-action ba-too-low1
  "I (Drone1) am too low"
  (fn [{:keys [altitude]}] (< altitude 1))
  (fn [navdata] (mdrone :drone1 :up 0.2)))

(def-goal g-cruising-altitude1
  "I (Drone1) want to get to a cruising altitude of 1 m"
  (fn [{:keys [altitude]}] (>= altitude 1))
  [ba-too-low1])

;;; Landing

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


;; Cruising Altitude

(def-belief-action ba-too-low2
  "I (Drone2) am too low"
  (fn [{:keys [altitude]}] (< altitude 1))
  (fn [navdata] (mdrone :drone2 :up 0.2)))

(def-goal g-cruising-altitude2
  "I (Drone2) want to get to a cruising altitude of 1 m"
  (fn [{:keys [altitude]}] (>= altitude 1))
  [ba-too-low2])

;;; Landing

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




;;;  initialization to run

(drone-initialize :drone1  "192.168.1.1"  default-at-port)
(set-current-goal-list drones :drone1 [g-take-off1 g-cruising-altitude1 g-land1])
(mdrone-init-navdata :drone1)
(start-stream :drone1)
(mdrone :drone1 :emergency)



;; (drone-initialize :drone1  "192.168.1.100"  default-at-port)
;; (set-current-goal-list drones :drone1 [g-take-off1 g-cruising-altitude1 g-land1])
;; (drone-initialize :drone2  "192.168.1.200"  default-at-port)
;; (set-current-goal-list drones :drone2 [g-take-off2 g-cruising-altitude2 g-land2])


;; (mdrone-init-navdata :drone1)
;; (start-stream :drone1)

;; (mdrone-init-navdata :drone2)
;; (start-stream :drone2)


;;  @(:current-belief (:drone2  @drones))
;; @(:current-goal (:drone2  @drones))
;; @(:current-goal-list (:drone2  @drones))
;; @(:nav-data (:drone2  @drones))
;;  @(:nav-data (:drone1  @drones))

;; ;; nav-agent
;; ;; (pst (first (agent-errors nav-agent)))

;; (mdrone :drone2 :take-off)
;; (mdrone :drone2 :land)

;; (mdrone :drone1 :take-off)
;; (mdrone :drone1 :land)

(end-navstream)




;;If running in the repl end the nav-stream when done


