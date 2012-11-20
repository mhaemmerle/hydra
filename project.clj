(defproject hydra "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.clojure/tools.logging "0.2.3"]
                 [lamina "0.5.0-SNAPSHOT"]
                 [aleph "0.3.0-beta7"]
                 [org.clojure/tools.nrepl "0.2.0-beta9"]
                 [org.mozilla/rhino "1.7R4"]
                 [evaljs "0.1.2"]]
  :dev-dependencies [[criterium "0.2.1"]]
  :plugins [[lein-marginalia "0.7.1"]]
  :jvm-opts ["-server"
             "-Djava.awt.headless=true"
             "-XX:+UseConcMarkSweepGC"
             "-XX:+UseParNewGC"
             "-d64"
             "-Xms4g"
             "-Xmx4g"
             "-XX:MaxPermSize=256m"
             "-XX:NewRatio=2"
             "-XX:ParallelGCThreads=8"
             "-XX:ParallelCMSThreads=8"
             "-XX:ConcGCThreads=8"]
  :main hydra.core)
