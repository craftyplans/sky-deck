(ns sky-deck.main
  (:gen-class)
  (:require [cambium.core :as log]
            [sky-deck.config :as config]
            [integrant.core :as ig]))

(defn -main
  [& args]
  (try (let [env (or (keyword (first args)) :production)
             system (config/new-system env)]
         (ig/init system)
         (log/info {:args args} "application-started"))
       @(promise)
       (catch Throwable e (log/error {:args args} e "application-error"))))
