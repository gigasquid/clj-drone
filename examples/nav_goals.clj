(ns clj-drone.example.nav-goals
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))

(set-log-data [:seq-num :control-state :altitude])

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

(def-belief-action ba-too-low
  "I am too low"
  (fn [{:keys [altitude]}] (< altitude 1))
  (fn [navdata] (drone :up 0.2)))

(def-goal g-cruising-altitude
  "I want to get to a cruising altitude of 1 m"
  (fn [{:keys [altitude]}] (>= altitude 1))
  [ba-too-low])

(def-belief-action ba-flying
  "I am flying"
  (fn [{:keys [control-state]}] (or (= control-state :hovering) (= control-state :flying)))
  (fn [navdata] (drone :land)))

(def-belief-action ba-landing
  "I am landing"
  (fn [{:keys [control-state]}] (= control-state :trans-landing))
  nil)

(def-goal g-land
  "I want to land"
  (fn [{:keys [control-state]}] (= control-state :landed))
  [ba-flying ba-landing])


(set-current-goal-list [g-take-off g-cruising-altitude g-land])

(drone-initialize)
(drone-init-navdata)
(end-navstream)
;(@nav-data :battery-level)
;(drone :land)
;(drone :emergency)
;(drone :flat-trim)




