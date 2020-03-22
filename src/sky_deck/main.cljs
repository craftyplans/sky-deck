(ns ^:figwheel-hooks sky-deck.main
  (:require
    [goog.dom :as gdom]
    [cljs.pprint :as pp]
    [re-frame.core :as rf]
    [devtools.core :as devtools]
    [reagent.core :as reagent :refer [atom]]
    [re-graph.core :as re-graph]
    [graphql-query.core :as graphql]
    [cljs.spec.alpha :as s]))

(devtools/install!)
(enable-console-print!)

(rf/reg-event-db
  ::on-mutate
  (fn [db [_ payload]]
    (js/console.log payload)
    (assoc db ::mutation payload)))

(defn join-battle
  []
  {:operation {:operation/type :mutation
               :operation/name "join_battle"}
   :variables [{:variable/name :$number
                :variable/type :Int!}]
   :queries   [[:join_battle {:number :$number}
                [:id
                 :type
                 :strength
                 :agility
                 :mind
                 :soul]]]})

(defn join-battle-form
  []
  [:form
   [:div.form-group
    [:input.form-control {:type "number" :name "battle-number"}]]
   [:button {:type "submit"
             :on-click (fn [event]
                          (.preventDefault event)
                         (rf/dispatch [::re-graph/mutate
                                       (graphql/graphql-query (join-battle))
                                       {:number 1}
                                       [::on-mutate]])
                         (js/console.log "hello" event))
             :class "btn btn-primary"} "Join Battle"]])

(defn index
  []
  [:div
   [:div [:h1 "Join a battle"]]
   [join-battle-form]])

(defn mount [el]
  (reagent/render-component [index] el))

(defn get-app-element
  []
  (.querySelector js/document "#app"))

(defn mount-app-element []
  (rf/dispatch [::re-graph/init
                {:ws-url "ws://localhost:5001/anonymous-graphql-stream-ws"
                 :http-url "http://localhost:5001/anonymous-graphql"}])
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
