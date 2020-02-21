(ns for-humans.success
  "Provides a common API for signalling success or failure with a result map.")

(def successful?
  "Returns true if the given result map signifies a successful
  result."
  ::success?)

(def get-reason
  "Returns the reason for the success or failure from the given result
  map."
  ::reason)

(def get-exception
  "Returns the exception value for a given failure result map."
  ::exception)

(defn succeed
  "Mark the given data map as a successful result, optionally giving a
  reason for the success to differentiate different success
  conditions."
  ([data]
   (assoc data
     ::success? true))
  ([data reason]
   (assoc data
     ::success? true
     ::reason reason)))

(defn ignore
  "Mark the given data map as one that was ignored, skipped, no-op.
  This is considered a successful result."
  [data]
  (succeed data ::ignored))

(defn ignored? [result]
  (and (successful? result) (= ::ignored (get-reason result))))

(defn fail
  "Mark the given data map as a failed result, giving a reason for the
  failure to differentiate different failure conditions. Providing an
  exception as a value is optional."
  ([data reason]
   (assoc data
     ::success? false
     ::reason reason))
  ([data reason exception]
   (assoc data
     ::success? false
     ::reason reason
     ::exception exception)))
