(defproject clj-drone "0.1.0-SNAPSHOT"
  :description "Clojure Control for the AR Drone"
  :url "https://github.com/gigasquid/clj-drone"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                 :plugins [[lein-midje "2.0.1"]]}})
