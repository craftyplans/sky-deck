(ns sky-deck.node
  (:require [sky-deck.global-id :as sd.global-id]
            [sky-deck.db :as sd.db]
            [sky-deck.db :as db]))

(defmulti node-by-id (fn [type id] type))

(defmethod node-by-id :Campaign
  [type id]
  {:select [:*]
   :from   [:campaign]
   :where  [:= :campaign/id id]})

(defn node
  [datasource global-id]
  (let [[type id] (sd.global-id/from-global-id global-id)]
    (db/execute-one-sql datasource (node-by-id type id))))
