(ns clj-drone.commands)

(def commands
  {
    :take-off        {:command-class "AT*REF" :command-bit-vec [9 18 20 22 24 28]}
    :land            {:command-class "AT*REF" :command-bit-vec [18 20 22 24 28]}
    :emergency       {:command-class "AT*REF" :command-bit-vec [8 18 20 22 24 28]}
    :spin-right      {:command-class "AT*PCMD" :command-vec [1 0 0 0 :v] :dir 1}
    :spin-left       {:command-class "AT*PCMD" :command-vec [1 0 0 0 :v] :dir -1}
    :up              {:command-class "AT*PCMD" :command-vec [1 0 0 :v 0] :dir 1}
    :down            {:command-class "AT*PCMD" :command-vec [1 0 0 :v 0] :dir -1}
    :tilt-back       {:command-class "AT*PCMD" :command-vec [1 0 :v 0 0] :dir 1}
    :tilt-front      {:command-class "AT*PCMD" :command-vec [1 0 :v 0 0] :dir -1}
    :tilt-right      {:command-class "AT*PCMD" :command-vec [1 :v 0 0 0] :dir 1}
    :tilt-left       {:command-class "AT*PCMD" :command-vec [1 :v 0 0 0] :dir -1}
    :hover           {:command-class "AT*PCMD" :command-vec [0 0 0 0 0] :dir 1}
    :fly             {:command-class "AT*PCMD" :command-vec [1 :v :w :x :y] :dir 1}
    :flat-trim       {:command-class "AT*FTRIM"}
    :reset-watchdog  {:command-class "AT*COMWDG"}
    :init-navdata    {:command-class "AT*CONFIG" :option "\"general:navdata_demo\""
                      :value "\"FALSE\""}
    :init-targeting  {:command-class "AT*CONFIG" :option "\"detect:detect_type\""
                      :value "\"10\""}
    :target-shell-h  {:command-class "AT*CONFIG" :option "\"detect:detections_select_h\""
                      :value "\"32\""}
    :target-roundel-v  {:command-class "AT*CONFIG" :option "\"detect:detections_select_v_hsync\""
                        :value "\"128\""}
    :target-color-green {:command-class "AT*CONFIG" :option "\"detect:enemy_colors\""
                         :value "\"1\""}
    :target-color-yellow {:command-class "AT*CONFIG" :option "\"detect:enemy_colors\""
                        :value "\"2\""}
    :target-color-blue {:command-class "AT*CONFIG" :option "\"detect:enemy_colors\""
                        :value "\"3\""}
    :anim-yaw-shake {:command-class "AT*CONFIG" :option "\"control:flight_anim\""
                     :value "\"8,2000\""}
    :anim-turnaround {:command-class "AT*CONFIG" :option "\"control:flight_anim\""
                      :value "\"6,5000\""}
    :anim-wave {:command-class "AT*CONFIG" :option "\"control:flight_anim\""
                :value "\"13,5000\""}
    :anim-double-phi-theta-mixed {:command-class "AT*CONFIG" :option "\"control:flight_anim\""
                                  :value "\"15,5000\""}
    :anim-flip-right {:command-class "AT*CONFIG" :option "\"control:flight_anim\""
                      :value "\"19,15\""}
    :control-ack     {:command-class "AT*CTRL" :value 0}
    })