(defproject clj-drone "0.1.0-SNAPSHOT"
  :description "Clojure Control for the AR Drone"
  :url "https://github.com/gigasquid/clj-drone"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [org.clojure/tools.logging "0.2.3"]
                 [ clj-logging-config "1.9.10"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                 :plugins [[lein-midje "2.0.1"]]}})
