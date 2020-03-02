(ns ^:figwheel-hooks sky-deck.main
  (:require
    [goog.dom :as gdom]
    [cljs.pprint :as pp]
    [re-frame.core :as rf]
    [devtools.core :as devtools]
    [reagent.core :as reagent :refer [atom]]
    [re-graph.core :as re-graph]
    [cljs.spec.alpha :as s]))

(devtools/install!)
(enable-console-print!)

(defn index
  []
  [:div [:p "hello"]])

(rf/dispatch [::re-graph/init {}])

(defn mount [el]
  (reagent/render-component [index] el))

(defn mount-app-element []
  (rf/dispatch-sync [::initialise-world])
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
