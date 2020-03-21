(ns sky-deck.executor
  (:require [integrant.core :as ig]
            [cambium.core :as log]
            [manifold.executor :as me])
  (:import (java.util.concurrent TimeUnit)))

;; taken from edge.executor
;; https://github.com/juxt/edge/blob/392fa203d3dfd6c1a245f22665c6b5c1e751014b/examples/main/src/edge/executor.clj

(defmethod ig/init-key :sky-deck/executor
  [_ _]
  (log/info {} "started-executor")
  (me/fixed-thread-executor 10))

(defmethod ig/halt-key! :sky-deck/executor
  [_ e]
  (when e
    (log/info {} "stopping-executor")
    (.shutdownNow e)
    (.awaitTermination e 5 TimeUnit/SECONDS)
    (log/info {} "executor-stopped")))
