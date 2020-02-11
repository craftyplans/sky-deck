(ns sky-deck.config
  (:require [clojure.java.io :as io]
            [cambium.core :as log]
            [integrant.core :as ig]
            [aero.core :as aero]))

(defmethod aero/reader 'ig/ref [_ _ value] (ig/ref value))

(defn load-config
  [profile]
  (aero/read-config (io/resource "config.edn") {:profile profile}))

(defn new-system
  [profile]
  (let [res (load-config profile)
        namespaces (vec (ig/load-namespaces res))]
    (log/info {:profile    profile
               :namespaces namespaces
               :config     (keys res)}
              "sky-deck/config-loaded")
    res))
