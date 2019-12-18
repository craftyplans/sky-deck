(ns ^:figwheel-hooks sky-deck.main
  (:require
    [goog.dom :as gdom]
    [cljs.pprint :as pp]
    [re-frame.core :as rf]
    [devtools.core :as devtools]
    [reagent.core :as reagent :refer [atom]]
    [cljs.spec.alpha :as s]))

(devtools/install!)
(enable-console-print!)

(s/def :battle/number number?)

(def default-db-value
  {:battle/number nil
   :form/input {:join-battle-inputs {}}})

(rf/reg-event-db
  ::initialise-world
  (fn [_ _]
    default-db-value))

(rf/reg-event-db
  ::join-battle
  (fn [db [_ battle-number]]
    (pp/pprint battle-number)
    (assoc db :battle/number battle-number)))

(rf/reg-event-db
  ::set-battle-number-input
  (fn [db [op value]]
    (assoc-in db [:form/input :join-battle-inputs] value)))

(rf/reg-sub
  ::set-battle-number-input
  (fn [db]
    (get-in db [:form/input :join-battle-inputs])))

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

(defn get-app-element
  []
  (gdom/getElement "app"))

(defn join-battle []
  [:div
   [:form
    [:div
     [:input
      {:type        "number"
       :placeholder "Battle Number"
       :on-change (fn [event]
                    (rf/dispatch [::set-battle-number-input (js/parseInt (.-value (.-target event)))]))
       :id          "battle-number"}]]
    [:div
     [:button
      {:on-click (fn [event]
                   (.preventDefault event)
                   (rf/dispatch [::join-battle @(rf/subscribe [::set-battle-number-input])]))}
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
  (rf/dispatch-sync [::initialise-world])
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
