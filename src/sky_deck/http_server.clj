(ns sky-deck.http-server
  (:require [integrant.core :as ig]
            [jsonista.core :as j]
            [schema.core :as s]
            [cambium.core :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [buddy.sign.jwt :as jwt]
            [schema.core :as s]
            [yada.yada :as yada])
  (:import (java.util Date)))

(def cors-configuration
  {:access-control {:allow-origin      "*"
                    :allow-credentials true
                    :allow-methods     #{:get :post :put :delete}
                    :allow-headers     ["Content-Type"
                                        "Authorization"
                                        "Set-Cookie"]}})


(def access-control-configuration
  {:access-control {:allow-origin      "*"
                    :allow-credentials true
                    :allow-methods     #{:get :post :put :delete}
                    :allow-headers     ["Content-Type"
                                        "Authorization"
                                        "Set-Cookie"]
                    :scheme            :sky-deck/auth
                    :authorization     {:methods {:get    :person
                                                  :post   :person
                                                  :put    :person
                                                  :delete :person}}}})

(defmethod ig/init-key :sky-deck/routes
  [_ _options]
  ["" [[true (yada/handler nil)]]])

(defmethod ig/init-key :sky-deck/http-server
  [_ options]
  (let [server (yada/listener (:sky-engine/routes options)
                              {:port (Integer/parseInt (:port options))})]
    (log/info {} "started-http-server")
    server))

(defmethod ig/halt-key! :sky-deck/http-server
  [_ server]
  (when-let [close (:close server)] (close))
  (log/info {} "stopped-http-server"))
