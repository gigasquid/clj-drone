(ns clj-drone.commands)

(def commands
  {
    :take-off  {:command-class "AT*REF" :command-bit-vec [9 18 20 22 24 28]}
    :land      {:command-class "AT*REF" :command-bit-vec [18 20 22 24 28]}
    :emergency {:command-class "AT*REF" :command-bit-vec [8 18 20 22 24 28]}
    })