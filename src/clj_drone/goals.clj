(ns clj-drone.goals)

(def current-belief (atom "None"))
(def current-goal (atom "None"))
(def current-goal-list (atom []))

(defmacro def-belief-action [bname belief-str belief-fn action-fn]
  `(def ~bname { :belief-str ~belief-str
                :belief ~belief-fn
                :action ~action-fn}))

(defmacro def-goal [gname goal-str goal-fn belief-actions]
  `(def ~gname { :goal-str ~goal-str
                :goal ~goal-fn
                :belief-actions ~belief-actions}))

(defn eval-belief-action [{:keys [belief-str belief action]} navdata]
  (when (belief navdata)
    (reset! current-belief belief-str)
    (action navdata)))

(defn eval-goal [{:keys [goal-str goal belief-actions]} navdata]
  (when goal
    (reset! current-goal goal-str)
    (if (goal navdata)
      (do
        (reset! current-belief (str "Achieved goal: " goal-str))
        :goal-reached)
      (doseq [ba belief-actions]
        (eval-belief-action ba navdata)))))

(defn eval-goal-list [goal-list navdata]
  (if (= :goal-reached (eval-goal (first goal-list) navdata))
    (rest goal-list)
    goal-list))

(defn set-current-goal-list [goal-list]
  (reset! current-goal-list goal-list))

(defn eval-current-goals [navdata]
  (set-current-goal-list (eval-goal-list @current-goal-list navdata)))

(defn log-goal-list [goal-list]
  (apply str (interpose ", " (map :goal-str goal-list))))

(defn log-goal-info []
  (str "goal list: " (log-goal-list @current-goal-list)
       " current-goal: " @current-goal
       " current-belief: " @current-belief))