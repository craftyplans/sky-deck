(ns sky-deck.test-http-auth
  (:require [clojure.test :as t]
            [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [jsonista.core :as j]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
            [juxt.iota :as i]
            [clojure.spec.gen.alpha :as gen]
            [ring.mock.request :as mock]
            [yada.yada :as yada]
            [byte-streams :as byte-streams]
            [sky-deck.config :as sd.config]
            [graphql-query.core :as graphql]
            [clojure.java.io :as io]))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)

(comment
  (yada/path-for (:sky-engine/routes system) :sky-deck.resource/login))

(defn init-test-system!
  []
  (let [config (sd.config/new-system :test)
        system (ig/init (dissoc config :sky-deck/http-server))]
    system))

(defmacro with-system
  [system-symbol & body]
  `(let [~system-symbol (init-test-system!)]
     (try
       (do ~@body)
       (finally (ig/halt! ~system-symbol)))))

(def json-mapper (j/object-mapper {:decode-key-fn keyword}))

(defn- request
  [{::keys [system method route-uri json-body]}]
  (let [handler (yada.handler/as-handler (:sky-deck/routes system))
        request (-> (mock/request method route-uri)
                    (mock/json-body json-body))]
    {::request  request
     ::response (when-let [response-defer (handler request)]
                  (let [response @response-defer
                        body (byte-streams/convert (:body response) String)
                        body-data (j/read-value body json-mapper)]
                    (assoc response :body body-data)))}))

(defn- graphql-request
  [{::keys [system query variables]}]
  (request {::route-uri "/anonymous-graphql"
            ::method :post
            ::json-body {:query (graphql/graphql-query query)
                         :variables variables}
            ::system system}))

(defn join-battle
  [battle-id]
  {:query {:operation {:operation/type :mutation
                       :operation/name "join_battle"}
           :variables [{:variable/name :$id
                        :variable/type :ID!}]
           :queries   [[:join_battle [:id]]]}
   :variables {:id battle-id}})

(t/deftest test-create-person
  (with-system
    system
    (let [session (request {::system system
                            ::method :get
                            ::route-uri "/"})

          graphql-session (graphql-request {::system system
                                            ::graphql-query {}
                                            ::graphql-variables {}})

          ]

      (t/is (= {} session)))))

#_(t/deftest test-create-campaign
  (let [])
  (t/is (false? true)))

#_(t/deftest test-basic-auth
  (t/is (false? true)))

#_(t/deftest test-anonymous-character-create
  (t/is (false? true)))
