(ns clj-drone.goals)

(defmacro def-belief-action [bname belief-str belief-fn action-fn]
  `(def ~bname { :belief-str ~belief-str
                :belief ~belief-fn
                :action ~action-fn}))

(defmacro def-goal [gname goal-str goal-fn belief-actions]
  `(def ~gname { :goal-str ~goal-str
                :goal ~goal-fn
                :belief-actions ~belief-actions}))

(defn set-current-belief [drones dname belief]
  (let [current-belief (:current-belief (dname @drones))]
    (reset! current-belief belief)))

(defn get-current-belief [drones dname]
  @(:current-belief (dname @drones)))

(defn set-current-goal [drones dname goal]
  (let [current-goal (:current-goal (dname @drones))]
    (reset! current-goal goal)))

(defn get-current-goal [drones dname]
  @(:current-goal (dname @drones)))

(defn set-current-goal-list [drones dname goal-list]
  (let [current-goal-list (:current-goal-list (dname @drones))]
    (reset! current-goal-list goal-list)))

(defn get-current-goal-list [drones dname]
  @(:current-goal-list (dname @drones)))

(defn eval-belief-action [{:keys [belief-str belief action]} navdata drones dname]
  (when (belief navdata)
    (set-current-belief drones dname belief-str)
    (when action (action navdata))))

(defn eval-goal [{:keys [goal-str goal belief-actions]} navdata drones name]
  (when goal
    (set-current-goal drones name goal-str)
    (if (goal navdata)
      (do
        (set-current-belief drones name (str "Achieved goal: " goal-str))
        :goal-reached)
      (doseq [ba belief-actions]
        (eval-belief-action ba navdata drones name)))))

(defn eval-goal-list [goal-list navdata drones name]
  (if (= :goal-reached (eval-goal (first goal-list) navdata drones name))
    (rest goal-list)
    goal-list))

(defn eval-current-goals [drones dname navdata]
  (set-current-goal-list drones
                         dname
                         (eval-goal-list (get-current-goal-list drones dname) navdata drones dname)))

(defn log-goal-list [goal-list]
  (apply str (interpose ", " (map :goal-str goal-list))))

(defn log-goal-info [drones dname]
  (str "goal list: " (log-goal-list (get-current-goal-list drones dname))
       " current-goal: " (get-current-goal drones dname)
       " current-belief: " (get-current-belief drones dname)))