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

(defn load-taps
  []
  (add-tap pp/pprint))

(defonce _taps (load-taps))

(defn get-app-element
  []
  (gdom/getElement "app"))

(defn get-current-battle
  [db]
  (get-in db [:battles (:current-battle-id db)]))

(defn get-current-hand
  [db]
  (when-let [battle (get-current-battle db)]
    (get-in battle [:hands (:current-hand-id battle)])))


(defn locked?
  [hand]
  (= (:state hand) :locked))

(def default-db-value
  {:action-types      {:quick-attack   {:name "Quick-Attack"}
                       :hard-attack    {:name "Hard Attack"}
                       :precise-attack {:name "Precise Attack"}
                       :cast-spell     {:name "Cast Spell"}
                       :quick-defend   {:name "Quick Defend"}
                       :hard-defend    {:name "Hard Defend"}
                       :precise-defend {:name "Precise Defend"}
                       :focus          {:name "Focus"}
                       :full           {:name "Full"}
                       :move           {:name "Move"}
                       :other          {:name "Other "}
                       :blink          {:name "Blink"}
                       :burst          {:name "Burst"}}

   :characters        {3 {:id 3 :name "Zape" :actions 2 :type :player}
                       4 {:id 4 :name "Sadie" :actions 2 :type :player}
                       6 {:id 6 :name "Duck" :actions 2 :type :player}
                       7 {:id 7 :name "Azagoth DuTrey" :actions 2 :type :npc}
                       8 {:id 8 :name "Professor Albert" :actions 2 :type :player}
                       9 {:id 9 :name "Hopes" :actions 2 :type :player}}

   :battles           {1 {:id            1
                          :state         :started           ;; :finished

                          :current-round-id 2
                          :current-hand-id 10

                          :hands         {10 {:id        10
                                              :character-id 3
                                              :state :open ;; :locked
                                              :actions   {}}}
                          :rounds        {2 {:id      2
                                             :state   :open ;; :started :locked :finished
                                             :actions []}}}}
   :current-battle-id nil
   :current-character-id 3})

(rf/reg-event-db
  ::initialise-world
  [rf/debug]
  (fn [_ _]
    default-db-value))

(rf/reg-event-db
  ::join-battle
  [rf/debug]
  (fn [db [_ battle-number]]
    (assoc db :current-battle-id battle-number)))

(rf/reg-event-db
  ::select-action
  [rf/debug]
  (fn [db [_operation battle-id hand-id action-type]]
    (update-in db [:battles battle-id :hands hand-id :actions action-type] (fnil inc 0))))

(rf/reg-event-db
  ::confirm-actions
  (fn [db [_operation battle-id hand-id]]
    (update-in db [:battles battle-id :hands hand-id] assoc :state :locked)))


(rf/reg-sub
  ::get-current-battle
  (fn [db _]
    (get-current-battle db)))

(rf/reg-sub
  ::get-action-types
  (fn [db _]
    (:action-types db)))

(rf/reg-sub
  ::get-current-hand
  (fn [db _]
    (get-current-hand db)))

(defn get-locked-actions
  [db]
  (when-let [current-hand (get-current-hand db)]
    (for [[action-id action-count] (:actions current-hand)]
      [action-id
       (assoc (get-in db [:action-types action-id]) :action-count action-count)])))

(rf/reg-sub
  ::get-locked-actions
  (fn [db _]
    (get-locked-actions db)))

(defn join-battle []
  (let [battle-input (atom nil)]
    [:div
     [:form
      [:div
       [:input
        {:type        "number"
         :name        "battle-number"
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

(defn display-actions
  [battle]
  (let [action-types @(rf/subscribe [::get-action-types])
        current-hand @(rf/subscribe [::get-current-hand])]
    [:div
     [:h2 ]
     [:ul
      (for [[id action-type] action-types]
        ^{:key id}
        [:li [:a
              {:href "#"
               :on-click (fn [event]
                           (rf/dispatch [::select-action (:id battle) (:id current-hand) id]))}
              (:name action-type) " "
              (get-in current-hand [:actions id])]])]]))

(defn confirm-actions
  [battle]
  (let [current-hand @(rf/subscribe [::get-current-hand])]
    [:div
     [:button
      {:disabled (locked? current-hand)
       :on-click (fn [event]
                   (.preventDefault event)
                   (rf/dispatch [::confirm-actions (:id battle) (:id current-hand)]))}
      "Ready!"]]))

(defn display-battle
  [battle]
  [:div
   [:h2 "Select Action"]
   [display-actions battle]
   [confirm-actions battle]])

(defn display-locked-actions
  [battle hand]
  (let [select-actions @(rf/subscribe [::get-locked-actions])]
    [:div
     [:h2 "Locked Actions"]
     [:ul
      (for [[id action] select-actions]
        ^{:key id}
        [:li [:p (:name action) " " (:action-count action)]])]]))

(defn index
  []
  [:div
   [:h1 "Sky"]
   (let [current-battle @(rf/subscribe [::get-current-battle])
         current-hand @(rf/subscribe [::get-current-hand])]
     (cond
       (locked? current-hand) [:div [display-locked-actions current-battle current-hand]]
       (some? current-battle) [:div [display-battle current-battle]]
       :else [:div [join-battle]]))])

(defn mount [el]
  (reagent/render-component [index] el))

(defn mount-app-element []
  (rf/dispatch-sync [::initialise-world])
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
