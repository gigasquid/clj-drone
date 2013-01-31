(ns clj-drone.goals-test
  (:use clojure.test
        midje.sweet
        clj-drone.goals))

(fact "about def-belief-action"
  (let [ belief-str "I am on the ground"
         belief-fn #(+ 1 1)
         action-str "Multipy"
         action-fn #(* 2 1)]
    (def-belief-action b-ground belief-str belief-fn action-str action-fn) => anything
    (b-ground :belief-str "I am on the ground")
    ((b-ground :belief-fn)) => 2
    (b-ground :action-str "Multiply")
    ((b-ground :action-fn)) => 2))

(fact "about def-goal"
  (let [ goal-str "Be Happy"
         goal-fn #(str ":)")
         smile (def-belief-action ba-sad "sad" #(str "Sad") "smile" #(str "Smile"))]
    (def-goal be-happy goal-str goal-fn [smile]) => anything
  (be-happy :goal-str) => "Be Happy"
  ((be-happy :goal-fn)) => ":)"
  (first (be-happy :belief-actions)) => smile))