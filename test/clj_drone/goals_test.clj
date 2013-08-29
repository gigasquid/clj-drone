(ns clj-drone.goals-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-drone.core :refer :all]
            [clj-drone.goals :refer :all]))

(defn belief-fn1 [navdata] (= 1 1))
(defn belief-fn2 [navdata] (= 1 2))
(defn action-fn1 [navdata] (str "My first action fn"))
(def-belief-action ba1 "belief 1" belief-fn1 action-fn1)
(def-belief-action ba2 "belief 2" belief-fn2 action-fn1)
(def-belief-action ba3 "belief 3" belief-fn1 nil)
(defn goal-fn1 [navdata] (= 2 2))
(defn goal-fn2 [navdata] (= 2 3))
(def-goal g1 "goal 1" goal-fn1 [ba1 ba2])
(def-goal g2 "goal 2" goal-fn2 [ba1 ba2])
(def-goal g3 "goal 3" goal-fn2 [ba1 ba2])

(defn reset-beliefs-goals []
  (reset! drones {:default { :current-belief (atom "None")
                            :current-goal (atom "None")
                            :current-goal-list (atom [])}}))

(reset-beliefs-goals)

(fact "about def-belief-action"
      (let [ belief-str "I am on the ground"
            belief-fn (fn [navdata] (+ 1 1))
            action-fn (fn [navdata] (+ 3 1))]
        (def-belief-action b-ground belief-str belief-fn action-fn) => anything
        (b-ground :belief-str "I am on the ground")
        ((b-ground :belief) {}) => 2
        ((b-ground :action) {}) => 4))

(fact "about def-goal"
      (let [ goal-str "Be Happy"
            goal (fn [navdata] (str ":)"))
            navdata {}
            smile (def-belief-action ba-sad "sad" (fn [nd] (str "Sad"))  (fn [nd] (str "Smile")))]
        (def-goal be-happy goal-str goal [smile]) => anything
        ((be-happy :goal) navdata) => ":)"
        (first (be-happy :belief-actions)) => smile))

(fact "eval-belief-action will execute the action if the belief-fn is true"
      (eval-belief-action ba1 {} drones :default) => "My first action fn"
      (get-current-belief drones :default) => "belief 1")

(fact "eval-belief-action can handle a nill action"
      (eval-belief-action ba3 {} drones :default) => nil)

(fact "eval-belief-action will not execute action if the belief-fn is false"
      (eval-belief-action ba2 {} drones :default) => nil
      (get-current-belief drones :default) => "None"
      (against-background (before :facts (reset-beliefs-goals))))

(fact "about eval-goal when the goal has been reached"
      (eval-goal g1 {} drones :default) => :goal-reached
      (get-current-belief drones :default) => "Achieved goal: goal 1"
      (get-current-goal drones :default) => "goal 1"
      (against-background (before :facts (reset-beliefs-goals))))

(fact "about eval-goal when the goal has not been reached"
      (eval-goal g2 {} drones :default) => nil
      (get-current-belief drones :default) => "belief 1"
      (get-current-goal drones :default) => "goal 2"
      (against-background (before :facts (reset-beliefs-goals))))

(fact "eval-goal handles a nil goal"
      (eval-goal nil {} drones :default) => nil)

(fact "eval-goal-list returns the same goal list if first goal has not been reached"
      (eval-goal-list [g2 g3] {} drones :default) => [g2 g3]
      (get-current-belief drones :default) => "belief 1"
      (get-current-goal drones :default) => "goal 2"
      (against-background (before :facts (reset-beliefs-goals))))

(fact "eval-goal-list returns the rest of the goal list if first goal has been reached"
      (eval-goal-list [g1 g2 g3] {} drones :default) => [g2 g3]
      (get-current-belief drones :default) => "Achieved goal: goal 1"
      (get-current-goal drones :default) => "goal 1"
      (against-background (before :facts (reset-beliefs-goals))))

(fact "eval-goal-list handles nil and  an empty list"
      (eval-goal-list nil {} drones :default) => nil
      (eval-goal-list [] {} drones :default) => [])

(fact "about set-current-goal-list"
      (set-current-goal-list drones :default [1 2 3]) => anything
      (get-current-goal-list drones :default) => [1 2 3])

(fact "about log-goal-list"
      (log-goal-list [g1 g2 g3]) => "goal 1, goal 2, goal 3")

(fact "about log-goal-info"
      (log-goal-info drones :default) => "goal list:  current-goal: None current-belief: None"
      (against-background (before :facts (reset-beliefs-goals))))
