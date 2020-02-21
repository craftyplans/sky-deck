(ns for-humans.system-helper
  (:require  [clojure.java.io :as io]
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
    (tap> {:for-humans/namespaces-loaded namespaces})
    res))
