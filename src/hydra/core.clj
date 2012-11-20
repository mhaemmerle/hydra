(ns hydra.core
  (:gen-class)
  (:require [clojure.tools.logging :as log])
  (:use [lamina core executor]
        [aleph http formats]
        criterium.core
        [cheshire.core :only [generate-string]])
  (:import [org.mozilla.javascript Context Scriptable NativeObject]))

(set! *warn-on-reflection* true)

(defonce script "function f(a) { return a.num + 1; }")

(defn event-loop
  [ch]
  (let [context (Context/enter)
        scope (.initStandardObjects context)]
    (.setOptimizationLevel context 2)
    (let [function (.compileFunction context scope script "<compiled>" 1 nil)]
      (loop []
        (let [native-object (NativeObject.)
              ;; num @(read-channel ch)]
              {:keys [chunk-channel num]} @(read-channel ch)]
          (.put native-object (name :num) native-object num)
          (let [result (.call function context scope nil (object-array [native-object]))]
            (enqueue-and-close chunk-channel {:result result})
            result))
        (recur)))))

(def event-channel (channel))

(defn- setup-event-loop
  [ch]
  (future (event-loop ch)))

(defn run-bench
  []
  (let [iterations 10000000
        start (. System (nanoTime))]
    (dotimes [i iterations]
      (enqueue event-channel (rand-int 100)))
    (let [elapsed-time (/ (double (- (. System (nanoTime)) start)) 1000000.0)
          ips (/ iterations (/ elapsed-time 1000.0))]
      (println (format "Elapsed time: %f msecs, %d ips" elapsed-time (Math/round ips))))))

(defn handler
  [ch request]
  (let [chunk-channel (map* generate-string (channel))]
    (enqueue event-channel {:chunk-channel chunk-channel :num (rand-int 1000)})
    (enqueue ch {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body chunk-channel})))

(defn start-server
  []
  (start-http-server handler {:port 8080}))
    
(defn at-exit
  [runnable]
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable runnable)))

(defn -main
  [& args]
  (let [shutdown-fn (start-server)]
    (at-exit shutdown-fn))
  (println "running..."))
