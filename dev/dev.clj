(ns dev
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [puget.printer]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
            [sky-deck.mutations :as sd.mutations]
            [sky-deck.queries :as sky-deck.queries]
            [sky-deck.graphql :as sd.graphql]
            [next.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as l]
            [graphql-query.core :as graphql]
            [honeysql.core :as sql]))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)

(defn load-tap [] (add-tap (bound-fn* puget.printer/cprint)))

(defonce taps (load-tap))

(def dev-db {:dbtype "postgresql" :dbname "sky_deck_development"})

(defn execute-map
  [ds sql-map]
  (let [sql-string (sql/format sql-map)]
    (jdbc/execute! ds sql-string)))

(def ep execute-map)
(def ds (jdbc/get-datasource dev-db))

(def default-actions
  {:quick-attack   {:id :quick-attack :name "Quick Attack"}
   :hard-attack    {:id :hard-attack :name "Hard Attack"}
   :precise-attack {:id :precise-attack :name "Precise Attack"}
   :cast-spell     {:id :cast-spell :name "Cast Spell"}
   :quick-defend   {:id :quick-defend :name "Quick Defend"}
   :hard-defend    {:id :hard-defend :name "Hard Defend"}
   :precise-defend {:id :precise-defend :name "Precise Defend"}
   :focus          {:id :focus :name "Focus"}
   :full           {:id :full :name "Full"}
   :move           {:id :move :name "Move"}
   :other          {:id :other :name "Other "}
   :blink          {:id :blink :name "Blink"}
   :burst          {:id :burst :name "Burst"}})

(defn generate-default-actions
  []
  (for [[action action-map] default-actions]
    (ep ds
        (sd.mutations/generate-action-type {:action-type-inputs {:name (:name action-map)
                                                                 :slug (name (:id action-map))}}))))

(def current-campaign-id #uuid "5072e832-2d86-11ea-b123-3f04c1c50b65")
(def eric-id #uuid"4f00d2e3-78b9-4379-ab95-f927be2882d0")
(def ivan-id #uuid"94fceaea-93be-494c-aa77-a9b089065d46")
(def session-id  #uuid"5dc4ed3e-2d89-11ea-816b-3fb3eded8071")

(def schema (sd.graphql/compile-dungeon-master-api))


(defn execute-graphql
  [ds ])

(comment

  (graphql/graphql-query
    {:operation {:operation/type :mutation
                 :operation/name "create_campaign"}
     :variables [{:variable/name :$name
                  :variable/type :String!}]
     :queries   [[:create_campaign {:name :$name} [:id]]]})


  )


(defn create-campaign
  [ds params]
  (l/execute schema (graphql/graphql-query
                      {:operation {:operation/type :mutation
                                   :operation/name "create_campaign"}
                       :variables [{:variable/name :$name
                                    :variable/type :String!}]
                       :queries   [[:create_campaign {:name :$name} [:id :number]]]})
             params
             {:sky-deck/datasource ds
              :sky-deck/auth {:person-id ivan-id}}))


"U2Vzc2lvbjpjMzM0YWY4ZS0yZjE4LTExZWEtYTE0YS1mYmM5ZmNmY2U3Njc="

(defn create-session
  [ds campaign-id]
  (l/execute schema (graphql/graphql-query
                      {:operation {:operation/type :mutation
                                   :operation/name "create_session"}
                       :variables [{:variable/name :$campaign_id
                                    :variable/type :String!}]
                       :queries [[:create_session {:campaign_id :$campaign_id} [:id [:campaign [:id]]]]]}

                      )
             {:campaign_id campaign-id}
             {:sky-deck/datasource ds
              :sky-deck/auth       {:person-id ivan-id}}

             ))

(defn create-battle
  [ds session-id]
  (l/execute schema (graphql/graphql-query
                      {:operation {:operation/type :mutation
                                   :operation/name "create_battle"}
                       :variables [{:variable/name :$campaign_id
                                    :variable/type :String!}]
                       :queries [[:create_session {:campaign_id :$campaign_id} [:id [:campaign [:id]]]]]})
             {:session_id session-id}
             {:sky-deck/datasource ds
              :sky-deck/auth       {:person-id ivan-id}}))

(defn list-campaigns
  [ds]
  (l/execute schema (graphql/graphql-query {:queries [[:list_campaigns [:id :number :state [:dungeon_master [:id :username :email]]]]]})
             nil
             {:sky-deck/datasource ds
              :sky-deck/auth {:person-id ivan-id}}))


(comment

  (sky-deck.queries/person-by-username ds "ivan")

  (sky-deck.queries/campaign-by-number ds 1)


  (sd.graphql/compile-dungeon-master-api)

  (create-campaign ds {:name "test"})

  (create-session ds "Q2FtcGFpZ246NzAwYzhhMjYtMmU2ZC0xMWVhLThhYTMtOTc5YzhlYjllMThi")


  (list-campaigns ds)


  )


(comment

  (def ds (jdbc/get-datasource db))
  (def eric-id (sd.mutations/new-id))

  (ep ds (sd.mutations/generate-person {:new-id ivan-id
                                        :person-inputs {:email    "iwillig@gmail.com"
                                                        :username "ivan"
                                                        :password "password"}}))

  (ep ds (sd.mutations/generate-person {:new-id eric-id
                                        :person-inputs {:email    "eric@example.com"
                                                        :username "eric"
                                                        :password "password"}}))

  (ep ds (sd.mutations/generate-campaign {:new-id current-campaign-id
                                          :campaign-inputs {:dungeon_master_id eric-id}}))

  (ep ds (sd.mutations/generate-character {:person-id #uuid "dd91ac10-2d83-11ea-abec-abc86e648df9"
                                           :character-inputs {:name "Zape"
                                                              :background "An angry monkey"
                                                              :hit_point_max 170
                                                              :hit_point_current (- 170 28)
                                                              :agility 8
                                                              :strength 12
                                                              :mind 8
                                                              :soul 6
                                                              :skill_points 0
                                                              :reputation 10
                                                              :divinity_points 10
                                                              :moments 6
                                                              :past_lives 4
                                                              :charges 4
                                                              :age 45}}))

  (ep ds (sd.mutations/generate-session {:campaign-id current-campaign-id}))

  (ep ds
      (sd.mutations/generate-battle {:campaign-id     current-campaign-id
                                     :session-id      #uuid"6fabbaaa-2d98-11ea-be2d-973175adc052"
                                     :initiated-by-id #uuid"6231623a-2d98-11ea-8c1b-43bffe3fe6dd"}))

  )


(comment

  (sql/format
    (sd.mutations/generate-person {:new-id        (sd.mutations/new-id)
                                   :person-inputs {:email    "iwillig@gmail.com"
                                                   :username "ivan"
                                                   :password "password"}}))

  (sd.mutations/generate-campaign {:campaign-inputs {}})

  (sql/format
    (sd.mutations/generate-action-type {:action-type-inputs {:name "Hard Attack"
                                                             :slug "hard-attack"}}))

  )
