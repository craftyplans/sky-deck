(ns sky-deck.auth
  (:require [integrant.core :as ig]
            [yada.security :as yada.security]
            [buddy.sign.jwt :as jwt]
            [clojure.edn :as edn]
            [cambium.core :as log]))

(defmethod ig/init-key :sky-deck/auth
  [_ options]
  (defmethod yada.security/verify :sky-deck/auth
    [yada-ctx _scheme]
    (let [headers (get-in yada-ctx [:request :headers])
          authorization (get headers "Authorization")
          claims (-> (jwt/decrypt authorization
                                  (:secret-key options)
                                  (:encryption options))
                     :claims
                     edn/read-string)]
      ;; TODO (Ivan) Add information to look up auth
      (log/info {:headers headers
                 :claims  claims}
                "sky-deck/auth"))))
