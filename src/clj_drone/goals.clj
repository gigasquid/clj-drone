(ns clj-drone.goals)

(def current-belief (atom "None"))
(def current-goal (atom "None"))

(defmacro def-belief-action [bname belief-str belief-fn action-fn]
  `(def ~bname { :belief-str ~belief-str
                 :belief-fn ~belief-fn
                 :action-fn ~action-fn}))

(defmacro def-goal [gname goal-str goal-fn belief-actions]
  `(def ~gname { :goal-str ~goal-str
                 :goal-fn ~goal-fn
                 :belief-actions ~belief-actions}))

(defn eval-belief-action [{:keys [belief-str belief-fn action-fn]}]
  (when (belief-fn)
    (reset! current-belief belief-str)
    (action-fn)))

(defn eval-goal [{:keys [goal-str goal-fn belief-actions]}]
  (when goal-fn
    (reset! current-goal goal-str)
    (if (goal-fn)
        :goal-reached
        (doseq [ba belief-actions]
          (eval-belief-action ba)))))

(defn eval-goal-list [goal-list]
  (if (= :goal-reached (eval-goal (first goal-list)))
      (rest goal-list)
      goal-list))