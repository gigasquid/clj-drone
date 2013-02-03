(ns clj-drone.example.nav-goals
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]
            [clj-drone.goals :refer :all]))


(set-log-data [:seq-num :control-state :altitude])

(def-belief-action ba-landed
  "I am landed"
  (fn [{:keys [control-state]}] (= control-state :landed))
  (fn [navdata] (drone :take-off)))

(def-belief-action ba-hovering
  "I am hovering"
  (fn [{:keys [control-state]}] (= control-state :hovering))
  nil)

(def-goal g-take-off
  "I want to fly."
  (fn [{:keys [control-state]}] (= control-state :hovering))
  [ba-landed ba-hovering])


(set-current-goal-list [g-take-off])


(drone-initialize)
(drone-init-navdata)
(drone :land)
(end-navstream)
(drone :emergency)
