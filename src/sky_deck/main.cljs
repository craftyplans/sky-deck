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

;"Quick Attack",
;"Hard Attack",
;"Precise Attack",
;"Cast Spell",
;"Quick Defend",
;"Hard Defend",
;"Precise Defend",
;"Focus",
;"Full",
;"Move",
;"Other",
;"Blink",
;"Burst"

(defn get-app-element
  []
  (gdom/getElement "app"))

(def default-db-value
  {:action-types {:quick-attack  {:name "Quick-Attack"}
                  :hard-attack  {:name "Hard Attack"}
                  :precise-attack {:name "Precise Attack"}
                  :cast-spell {:name "Cast Spell"}
                  :quick-defend {:name "Quick Defend"}
                  :hard-defend {:name  "Hard Defend"}
                  :precise-defend {:name "Precise Defend"}
                  :focus {:name "Focus"}
                  :full {:name "Full"}
                  :move  {:name "Move"}
                  :other {:name "Other "}
                  :blink {:name "Blink"}
                  :burst {:name "Burst"}}

   :characters {3  {:id 3 :name "Zape" :actions 2 :type :player}
                4  {:id 4 :name "Sadie" :actions 2 :type :player}
                6  {:id 6 :name "Duck" :actions 2 :type :player}
                7  {:id 7 :name "Azagoth DuTrey" :actions 2 :type :npc}}

   :battles  {1 {:id 1
                 :state :started  ;; finished
                 :current-round 2
                 :rounds
                 [{:id 2
                 :state :open ;; started locked finished
                 :actions [{:id 5
                            :type :hard-attack
                            :source 3
                            :target 4}]}]}}
   :current-battle-id nil})

(rf/reg-event-db
  ::initialise-world
  (fn [_ _]
    default-db-value))

(rf/reg-event-db
  ::join-battle
  (fn [db [_ battle-number]]
    (assoc db :sky-deck/battle-current-number battle-number)))

(rf/reg-sub
  ::get-current-battle
  (fn [db _]
    (get-in db [:battles (:current-battle-id db)])))

(rf/reg-sub
  ::current-battle
  (fn [_ _]
    (rf/subscribe [::get-current-battle])))


(defn join-battle []
  (let [battle-input (atom nil)]
    [:div
     [:form
      [:div
       [:input
        {:type        "number"
         :name         "battle-number"
         :placeholder "Battle Number"
         :on-change   (fn [event]
                        (let [battle-number (js/parseInt (.-value (.-target event)))]
                          (reset! battle-input battle-number)))}]]
      [:div
       [:button
        {:on-click (fn [event]
                     (.preventDefault event)
                     (rf/dispatch [::join-battle @battle-input]))}
        "Join Battle!"]]]]))

(defn index
  []
  (if-let [current-battle @(rf/subscribe [::current-battle])]
    [:div
     [join-battle]]
    [:div "Battle"]))

(defn mount [el]
  (reagent/render-component [index] el))

(defn mount-app-element []
  (rf/dispatch-sync [::initialise-world])
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
