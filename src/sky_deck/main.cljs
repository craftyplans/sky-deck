(ns ^:figwheel-hooks sky-deck.main
  (:require
   [goog.dom :as gdom]
   [re-frame.core :as rf]
   [devtools.core :as devtools]
   [reagent.core :as reagent :refer [atom]]))

(devtools/install!)
(enable-console-print!)

(def round-states #{:round.state/un-started
                    :round.state/locked-actions})

(def actions [{:action/name :quick-attack}
              {:action/name :hard-attack}
              {:action/name :precise-attack}
              {:action/name :cast-spell}
              {:action/name :quick-defend}
              {:action/name :hard-defend}
              {:action/name :precise-defend}
              {:action/name :focus}
              {:action/name :full}
              {:action/name :move}
              {:action/name :other}
              {:action/name :blink}
              {:action/name :burst}])

(def round-state->title
  {:round.state/select-actions "Select Actions"
   :round.state/join-battle "Welcome to Sky"})

(defn gen-select-actions
  []
  {:round/state :round.state/select-actions
   :round/selected-actions {}})

(defonce app-state (atom {:battle/number nil
                          :battle/current-round {:round/state :round.state/join-battle}}))

(defn get-app-element
  []
  (gdom/getElement "app"))

(defn join-battle []
  [:div
   [:form
    [:div
     [:input
      {:type        "text"
       :placeholder "Battle Number"
       :id          "battle-number"}]]
    [:div
     [:button
      {:on-click (fn [event]
                   (.preventDefault event)
                   (cljs.pprint/pprint [event]))}
      "Join Battle!"]]]])

(defn list-actions
  []
  (let []))

(defn index
  []
  (let []
    [:div
     [join-battle]]))

(defn mount [el]
  (reagent/render-component [index] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
