(ns sky-deck.test-runner
  (:require
    [figwheel.main.testing :refer [run-tests-async]]
    [sky-deck.core-test]))

(defn -main [& args]
  (run-tests-async 5000))
