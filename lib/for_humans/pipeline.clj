(ns for-humans.pipeline)

(defn- stopped? [res]
  (and (vector? res)
       (= ::stop (first res))))

(defn stop
  "Returns a stopped tuple indicating that the current iteration is the last one."
  [res]
  [::stop res])

(defn process-pipeline
  "Passes context through a sequence of functions in order.
  If a stage function returns the stop tuple, then subsequent steps are not
  executed and the value is returned.
  "
  [init stages]
  (reduce
    (fn [acc stage]
      (let [result (stage acc)]
        (if (stopped? result)
          (reduced (second result))
          result)))
    init
    stages))
