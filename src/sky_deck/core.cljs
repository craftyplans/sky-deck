(ns ^:figwheel-hooks sky-deck.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]))

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
   [:form.bg-white.shadow-md.rounded.px-8.pt-6.pb-8.mb-4

    [:div.mb-4
     [:input.shadow.appearance-none.border.rounded.w-full.py-2.px-3.text-gray-700.leading-tight.focus:outline-none.focus:shadow-outline
      {:type        "text"
       :placeholder "Battle Number"
       :id          "battle-number"}]]

    [:div.mb-4
     [:button.shadow.bg-purple-500.hover:bg-purple-400.text-white.font-bold.py-2.px-4.rounded
      {:on-click (fn [event]
                   (.preventDefault event)
                   (swap! app-state merge {:battle/number 1
                                           :battle/current-round (gen-select-actions)}))}
      "Join Battle!"]]]])

(defn list-actions
  []
  (let [round (:battle/current-round @app-state)]
    [:div
     [:div.p-4
      (for [action actions]
        ^{:key (:action/name action)}
        [:span.block.text-center.bg-blue-200.px-4.py-2.mt-2.border-solid.border-2.border-blue-600.shadow-lg
         {:on-click (fn [_event]
                      (swap! app-state update-in [:battle/current-round :round/selected-actions (:action/name action)]
                             (fn [current] (if current
                                             (inc current)
                                             1))))}
         [:p (name (:action/name action))]
         (when-let [selected-info (get-in round [:round/selected-actions (:action/name action)])]
           [:p selected-info])])]
     [:div
      [:button.shadow.bg-green-500.hover:bg-green-400.text-white.font-bold.py-2.px-4.rounded
       "Ready"]]]))

(defn index
  []
  (let [battle-number (:battle/number @app-state)
        current-round (:battle/current-round @app-state)
        title         (round-state->title (:round/state current-round))]
    [:div.container.mx-auto.px-4
     [:div.bg-gray-400 [:p.text-center.text-5xl title]]
     (if (some? battle-number)
       [list-actions]
       [join-battle])]))


(defn mount [el]
  (reagent/render-component [index] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
