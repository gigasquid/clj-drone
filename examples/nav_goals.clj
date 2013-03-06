(ns clj-drone.example.nav-goals
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))

;Logging goes to logs/drone.log

(set-log-data [:seq-num :control-state :altitude])

;;;Taking off

(def-belief-action ba-landed
  "I am landed"
  (fn [{:keys [control-state]}] (= control-state :landed))
  (fn [navdata] (drone :take-off)))

(def-belief-action ba-taking-off
  "I am taking off"
  (fn [{:keys [control-state]}] (= control-state :trans-takeoff))
  nil)

(def-goal g-take-off
  "I want to fly."
  (fn [{:keys [control-state]}] (= control-state :hovering))
  [ba-landed ba-taking-off])


;; Cruising Altitude

(def-belief-action ba-too-low
  "I am too low"
  (fn [{:keys [altitude]}] (< altitude 1))
  (fn [navdata] (drone :up 0.2)))

(def-goal g-cruising-altitude
  "I want to get to a cruising altitude of 1 m"
  (fn [{:keys [altitude]}] (>= altitude 1))
  [ba-too-low])

;;; Landing

(def-belief-action ba-flying
  "I am flying"
  (fn [{:keys [control-state]}] (or (= control-state :hovering) (= control-state :flying)))
  (fn [navdata] (do
                 (drone :land))))

(def-belief-action ba-landing
  "I am landing"
  (fn [{:keys [control-state]}] (= control-state :trans-landing))
  nil)

(def-goal g-land
  "I want to land"
  (fn [{:keys [control-state]}] (= control-state :landed))
  [ba-flying ba-landing])

;; Detecting Target


(def-belief-action ba-no-detect-vertical-target
  "I do not see a vertical target"
  (fn [{:keys [targets-num]}]
    (or (nil? targets-num) (= targets-num 0)))
  (drone :hover-on-roundel))

(def-goal g-detect-target
  "I want to detect the vertical target"
  (fn [{:keys [targets-num]}] (> targets-num 0))
  [ba-no-detect-vertical-target])

;; Hover on target mode for 1 minutes
(def-belief-action ba-hovering-on-target
  "I am starting to follow the target"
  (fn [{:keys [timer-start]}] (nil? timer-start))
  (fn [navdata]
    (swap! nav-data merge {:timer-start (.getTime (new java.util.Date))})))

(def-belief-action ba-following-target
  "I have been following the target for under 1 minutes"
  (fn [{:keys [timer-start]}]
    (when timer-start
      (let [time-elapsed  (- (.getTime (new java.util.Date)) timer-start)]
        (swap! nav-data merge {:timer-elapsed  time-elapsed})
        (< time-elapsed 20000))))
  nil)

(def-goal g-follow-target
  "I want to follow the target for 1 minutes"
  (fn [{:keys [timer-elapsed]}]
    (when timer-elapsed
      (> timer-elapsed 20000)))
  [ba-hovering-on-target ba-following-target])


;;;; define the goal list

(set-current-goal-list [g-take-off g-cruising-altitude g-detect-target
                        g-follow-target g-land])


;;;  initialization to run

(drone-initialize)
(drone :target-roundel-v)
;; the drone will look for the black and white roundel on the vertical camera
(drone-init-navdata)
(end-navstream)  ;;If running in the repl end the nav-stream when done


;;(drone-do-for 2  :tilt-front 0.2)

(drone :land)
@nav-data
@current-belief
@current-goal-list
(:timer-start @nav-data)
(pst (first (agent-errors nav-agent)) )
ba-no-detect-vertical-target
((:belief ba-no-detect-vertical-target) @nav-data)
(agent-errors nav-agent)

(restart-agent nav-agent {})
(swap! nav-data {})
(:timer-elapsed @nav-data)







