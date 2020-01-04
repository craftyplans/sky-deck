(ns sky-deck.node
  (:require [sky-deck.global-id :as sd.global-id]
            []))

(defmulti node-by-id (fn [type id] type))

(defmethod node-by-id :Campaign [type id]
  {:select [:*]
   :from :campaign
   :where [:= :campaign/id id]})


(defn node
  [datasource global-id]
  (let [[type id] (sd.global-id/from-global-id global-id)]
    [type id]))
