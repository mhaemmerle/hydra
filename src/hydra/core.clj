(ns hydra.core
  (:gen-class)
  (:require [clojure.tools.logging :as log])
  (:use [lamina core executor]
        [aleph http formats]
        criterium.core
        [cheshire.core :only [generate-string]])
  (:import [org.mozilla.javascript Context Scriptable NativeObject NativeArray]))

(set! *warn-on-reflection* true)

(defonce file-name "resources/map.js")
(defonce event-channel (channel))

(defn create-map-entity
  [[x y]]
  (let [entity (NativeObject.)]
    (.put entity (name :x) entity x)
    (.put entity (name :y) entity y)
    entity))

(defn create-game-map
  []
  (let [coords [[0 0] [0 1] [0 2] [0 3]]]
    (NativeArray. ^objects (into-array Object (map create-map-entity coords)))))

(defn event-loop
  [ch]
  (let [context (Context/enter)
        scope (.initStandardObjects context)
        script (slurp file-name)]
    (.setOptimizationLevel context 2)
    (let [function (.compileFunction context scope script "<compiled>" 1 nil)]
      (loop []
        (let [state (NativeObject.)
              game-map (create-game-map)
              {:keys [chunk-channel num]} @(read-channel ch)]
          (.put state (name :map) state game-map)
          (let [result (.call function context scope nil (object-array [state 0 0]))
                response (.get ^NativeObject result (name :result))]
            (when chunk-channel
              (enqueue-and-close chunk-channel {:result response}))
            result))
        (recur)))))

(defn- setup-event-loop
  [ch]
  (future (event-loop ch)))

(defn run-bench
  []
  (let [iterations 10000000
        start (System/nanoTime)]
    (dotimes [i iterations]
      (enqueue event-channel {:num (rand-int 100)}))
    (let [elapsed-time (/ (double (- (System/nanoTime) start)) 1000000.0)
          ips (/ iterations (/ elapsed-time 1000.0))
          ips-rounded (Math/round ips)]
      (println (format "Elapsed time: %f msecs, %d ips" elapsed-time ips-rounded)))))

(defn handler
  [ch request]
  (let [chunk-channel (map* generate-string (channel))]
    (enqueue event-channel {:chunk-channel chunk-channel :num (rand-int 1000)})
    (enqueue ch {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body chunk-channel})))

(defn start-server
  []
  (setup-event-loop event-channel)
  (start-http-server handler {:port 8080}))
    
(defn at-exit
  [runnable]
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable runnable)))

(defn -main
  [& args]
  (let [shutdown-fn (start-server)]
    (at-exit shutdown-fn))
  (println "running..."))
