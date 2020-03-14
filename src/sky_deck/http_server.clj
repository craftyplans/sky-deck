(ns sky-deck.http-server
  (:require [integrant.core :as ig]
            [jsonista.core :as j]
            [schema.core :as s]
            [cambium.core :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [buddy.sign.jwt :as jwt]
            [schema.core :as s]
            [sky-deck.queries :as sd.queries]
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

(defn login
  [ctx request]
  (let [parameters (get-in request [:parameters :body])
        username (:username parameters)
        password (:password parameters)]
    (log/info {:username username} "login-request")
    (merge
     (:response request)
     (if-let [person (sd.queries/person-by-username (:sky-deck/db ctx)
                                                    username)]
       (if (buddy.hashers/check password (:password person))
         (let [token (jwt/encrypt {:claims (pr-str {:person (select-keys
                                                             person
                                                             [:person/id
                                                              :person/username])
                                                    :issued-at (Date.)
                                                    :roles #{:person}})}
                                  (:secret-key (:sky-deck/auth ctx))
                                  (:encryption (:sky-deck/auth ctx)))]
           {:status 201
            :body   {:token token}})
         {:body   {:message "unauthorized"}
          :status 401})
       {:body   {:message "unauthorized"}
        :status 401}))))

(defn generate-login
  [ctx]
  (yada/resource (-> {:id      :sky-deck.resource/login
                      :methods {:post {:consumes   "application/json"
                                       :produces   "application/json"
                                       :parameters {:body {:username s/Str
                                                           :password s/Str}}
                                       :response   (partial login ctx)}}}
                     (merge cors-configuration))))

(defn generate-graphql-endpoint
  [schema context]
  (-> {:id :sky-deck.resource/graphql-endpoint
       :methods
       {:post {:consumes "application/json"
               :produces "application/json"
               :response
               (fn [request]
                 (let [body (:body request)
                       query (:query body)
                       variables (if (string? (:variables body))
                                   (j/read-value (:variables body)
                                                 (j/object-mapper
                                                  {:decode-key-fn keyword}))
                                   (:variables body))
                       response (lacinia/execute
                                 schema
                                 query
                                 variables
                                 (assoc context
                                        :com.walmartlabs.lacinia/enable-timing?
                                        true))]
                   (dissoc response :extensions)))}}}
      (yada/resource)))

(defmethod ig/init-key :sky-deck/routes
  [_ options]
  [""
   [["/"
     (yada/resource {:id      :sky-deck.resource/index
                     :methods {:get {:produces "application/json"
                                     :consumes "application/json"
                                     :response (fn [ctx] {:hello "world"})}}})]
    ["/login" (generate-login options)]
    ["/anonymous-graphql"
     (generate-graphql-endpoint (get-in options
                                        [:graphql :sky-deck/anonymous-schema])
                                options)]
    ["/dungeon-master-graphql" (yada/handler {:hello "dungeon master graphql"})]
    ["/authenticated-player-graphql"
     (yada/handler {:hello "auth player graphql"})]
    [true (yada/handler nil)]]])

(defmethod ig/init-key :sky-deck/http-server
  [_ options]
  (let [server (yada/listener (:sky-deck/routes options)
                              {:port (Integer/parseInt (:port options))})]
    (log/info {:options options} "started-http-server")
    server))

(defmethod ig/halt-key! :sky-deck/http-server
  [_ server]
  (when-let [close (:close server)] (close))
  (log/info {} "stopped-http-server"))
