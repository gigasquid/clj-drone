(ns clj-drone.commands)

(def commands
  {
    :take-off   {:command-class "AT*REF" :command-bit-vec [9 18 20 22 24 28]}
    :land       {:command-class "AT*REF" :command-bit-vec [18 20 22 24 28]}
    :emergency  {:command-class "AT*REF" :command-bit-vec [8 18 20 22 24 28]}
    :spin-right {:command-class "AT*PCMD":command-bit-vec [] :command-vec [1 0 0 0 :x]}
    })