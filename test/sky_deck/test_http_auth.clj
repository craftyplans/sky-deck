(ns sky-deck.test-http-auth
  (:require [clojure.test :as t]
            [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [jsonista.core :as j]
            [bidi.bidi :as bidi]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
    #_[juxt.iota :as i]
            [clojure.spec.gen.alpha :as gen]
            [ring.mock.request :as mock]
    #_[yada.yada :as yada]
            [yada.handler :as yada.handler]
            [byte-streams :as byte-streams]
            [sky-deck.config :as sd.config]
            [sky-deck.mutations :as sd.mutations]
            [graphql-query.core :as graphql]
    #_[clojure.java.io :as io]
            [medley.core :as m]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [sky-deck.db :as db])
  (:import (java.util UUID)))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)


(defn init-test-system!
  []
  (let [config (sd.config/new-system :test)
        ;; Remove http server in test mode to prevent yada from starting a real http server
        system (ig/init (dissoc config :sky-deck/http-server))]
    system))

(defn- with-system-fn
  [fn]
  (let [system (init-test-system!)]
    (try
      (fn system)
      (finally (ig/halt! system)))))

(defmacro with-system
  [system-symbol & body]
  `(let [~system-symbol (init-test-system!)]
     (try
       (do ~@body)
       (finally (ig/halt! ~system-symbol)))))

(def json-mapper (j/object-mapper {:decode-key-fn keyword}))

(defn- response-ok?
  [request-session]
  (= (get-in request-session [::response :status]) 200))

(defn- response-json?
  [response]
  (str/includes? (ring/get-header response "content-type") "application/json"))

(defn- parse-json-response
  [defer-response]
  (when defer-response
    (let [response @defer-response
          body (byte-streams/convert (:body response) String)]
      (m/assoc-some response :body-data
                    (when (response-json? response)
                      (j/read-value body json-mapper))))))

(defn- request
  [{::keys [system method route-name route-uri json-body graphql-str] :as opts}]
  (let [routes (:sky-deck/routes system)
        handler (yada.handler/as-handler routes)
        request (-> (mock/request method (if route-name
                                           (bidi/path-for routes route-name)
                                           route-uri))
                    (mock/json-body json-body))
        response (handler request)]
    {::request  request
     ::request-opts {::graphql-str graphql-str}
     ::response (parse-json-response response)}))

(defn- graphql-request
  [{::keys [system query variables]}]
  (let [graphql-str (graphql/graphql-query query)]
    (request {::route-uri "/anonymous-graphql"
              ::graphql-str graphql-str
              ::method    :post
              ::json-body {:query     graphql-str
                           :variables variables}
              ::system    system})))

(defn join-battle
  []
  {:operation {:operation/type :mutation
               :operation/name "join_battle"}
   :variables [{:variable/name :$number
                :variable/type :Int!}]
   :queries   [[:join_battle {:number :$number}
                [:id
                 :type
                 :strength
                 :agility
                 :mind
                 :soul]]]})

(def battle-attributes
  [:id
   :number
   :created_at
   :updated_at
   [:participants [:id :name :soul :mind :strength]]
   [:campaign [:id]]])

(defn find-battle-by-number
  []
  {:operation {:operation/type :query
               :operation/name "find_battle"}
   :variables [{:variable/name :$number
                :variable/type :Int!}]
   :queries [[:find_battle {:number :$number} battle-attributes]]})

(defn gen-default-tx-data
  []
  (let [person-id (UUID/randomUUID)

        person (sd.mutations/generate-person {:new-id person-id
                                              :person-inputs
                                                      (assoc (gen/generate (s/gen ::sd.mutations/person-inputs))
                                                        :password "password")})

        character-id (UUID/randomUUID)

        character (sd.mutations/generate-character {:person-id person-id
                                                    :new-id character-id
                                                    :character-inputs (gen/generate (s/gen ::sd.mutations/character-inputs))})


        campaign-id (UUID/randomUUID)
        default-campaign (sd.mutations/generate-campaign
                           {:new-id campaign-id
                            :campaign-inputs
                            {:dungeon_master_id person-id}})

        session-id (UUID/randomUUID)
        session (sd.mutations/generate-session {:new-id session-id
                                                :campaign-id campaign-id})

        battle-id (sd.mutations/new-id)
        battle (sd.mutations/generate-battle {:new-id battle-id
                                              :campaign-id campaign-id
                                              :session-id session-id
                                              :initiated-by-id character-id})

        battle-participant (sd.mutations/generate-battle-participant
                             {:battle-id battle-id
                              :character-id character-id})]

    ;; This is kind of lame. Order is important!
    {::default-person person
     ::default-character character
     ::default-campaign default-campaign
     ::default-session session
     ::default-battle battle
     ::participant battle-participant}))

(defn init-db
  [system]
  (let [tx-data (gen-default-tx-data)]
    (sd.mutations/transact-map system tx-data)))

(t/deftest test-create-person
  (with-system-fn
    (fn [system]
      (let [init-objects (init-db system)

            session (request {::system     system
                              ::method     :get
                              ::route-name :sky-deck.resource/index})

            battle-number (get-in init-objects [::default-battle :battle/number])

            battle-session (graphql-request {::system system
                                             ::query (find-battle-by-number)
                                             ::variables {:number battle-number}})

            ;graphql-session (graphql-request {::system    system
            ;                                  ::query     (join-battle)
            ;                                  ::variables {:number nil}})

            ]

        #_(clojure.pprint/pprint [graphql-session])
        #_(t/is (response-ok? session))
        (t/is (= "" battle-number))
        (t/is (= {} (get-in battle-session [::response :body-data :data])))
        #_(t/is (= {} init-objects))

        #_(t/is (= {} session-login))
        #_(t/is (not (response-ok? graphql-session)))

        ))))

#_(t/deftest test-create-campaign
    (let [])
    (t/is (false? true)))

#_(t/deftest test-basic-auth
    (t/is (false? true)))

#_(t/deftest test-anonymous-character-create
    (t/is (false? true)))
