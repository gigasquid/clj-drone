(ns clj-drone.goals)

(defmacro def-belief-action [bname belief-str belief-fn action-str action-fn]
  `(def ~bname { :belief-str ~belief-str
                 :belief-fn ~belief-fn
                 :action-str ~action-str
                 :action-fn ~action-fn}))

(defmacro def-goal [gname goal-str goal-fn belief-actions]
  `(def ~gname { :goal-str ~goal-str
                 :goal-fn ~goal-fn
                 :belief-actions ~belief-actions}))
