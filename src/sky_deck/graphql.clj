(ns sky-deck.graphql
  (:require
    [com.walmartlabs.lacinia.resolve :as lacinia-resolve]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [clojure.string :as str]
    [next.jdbc :as jdbc]
    [honeysql-postgres.format]
    [honeysql-postgres.helpers]
    [honeysql.core :as sql]
    [sky-deck.global-id :as sd.global-id]
    [sky-deck.node :as sd.node]
    [sky-deck.queries :as sd.queries]
    [sky-deck.mutations :as sd.mutations])
  (:import (java.util Base64)))

(def shared-interfaces
  {:Node {:fields {:id {:type '(non-null ID)}}}})

(def shared-objects
  {:Person {:implements [:Node]
            :fields {:id {:type '(non-null ID)
                          :resolve (fn [_ _ value]
                                     (sd.global-id/to-global-id :Person (:person/id value)))}
                     :username {:type 'String
                                :resolve (fn [_ _ value] (:person/username value))}
                     :email {:type 'String
                             :resolve (fn [_ _ value] (:person/email value))}}}

   :Viewer {:implements [:Node]
            :fields {:id {:type '(non-null ID)}}}

   :Arc {:implements [:Node]
         :fields {:id {:type '(non-null ID)}
                  :name {:type 'String}
                  :description {:type 'String}
                  :campaign {:type :Campaign}}}

   :ActionType {:implements [:Node]
                :fields {:id {:type '(non-null ID)}
                         :name {:type 'String}
                         :slug {:type 'String}}}

   :Session {:implements [:Node]
             :fields {:id {:type '(non-null ID)
                           :resolve (fn [_ _ value]
                                      (sd.global-id/to-global-id :Session (:session/id value)))}
                      :campaign {:type :Campaign}}}

   :Battle {:implements [:Node]
            :fields {:id {:type '(non-null ID)}
                     :number {:type 'Int}
                     :state {:type 'String}
                     :session {:type :Session}
                     :campaign {:type :Campaign}
                     :participants {:type '(list :Character)}}}

   :Round {:implements [:Node]
           :fields {:id {:type '(non-null ID)}
                    :state {:type 'String}
                    :battle {:type :Battle}
                    :campaign {:type :Campaign}
                    :hands {:type '(list :Hand)}}}

   :Hand {:implements [:Node]
          :fields {:id {:type '(non-null ID)}
                   :round {:type :Round}
                   :character {:type :Character}
                   :battle {:type :Battle}
                   :state {:type 'String}}}

   :HandAction {:implements [:Node]
                :fields {:id {:type '(non-null ID)}
                         :hand {:type :Hand}
                         :action_type {:type :ActionType}
                         :target {:type :Character}}}

   ;; TODO (Ivan) Figure out better way of mapping namespaced keywords to graphql
   ;; Also, how do we use pathom
   :Campaign {:implements [:Node]
              :fields {:id {:type '(non-null ID)
                            :resolve (fn [_ _ value]
                                       (sd.global-id/to-global-id :Campaign (:campaign/id value)))}
                       :number {:type 'Int
                                :resolve (fn [_ _ value]
                                           (:campaign/number value))}
                       :state {:type 'String}
                       :dungeon_master {:type :Person
                                        :resolve (fn [{:sky-deck/keys [datasource auth]} _ value]
                                                   (sky-deck.queries/person-by-id
                                                     datasource
                                                     (:campaign/dungeon_master_id value)))}
                       :players {:type '(list :Character)}}}

   :Character {:implements [:Node]
               :fields {:id {:type '(non-null ID)
                             :resolve (fn [_ _ value]
                                        (sd.global-id/to-global-id :Character (:character/id value)))}
                        :name {:type 'String}
                        :type {:type 'String}
                        :hit_point_max {:type 'Int}
                        :hit_point_current {:type 'Int}
                        :age {:type 'Int}
                        :agility {:type 'Int}
                        :strength {:type 'Int}
                        :mind {:type 'Int}
                        :soul {:type 'Int}
                        :skill_points {:type 'Int}
                        :reputation {:type 'Int}
                        :master_points {:type 'Int}
                        :divinity_points {:type 'Int}
                        :moments {:type 'Int}
                        :past_lives {:type 'Int}
                        :charges {:type 'Int}
                        :background {:type 'String}
                        :action_types {:type '(list :ActionType)}}}})

(def dungeon-master-api-schema
  {:objects       shared-objects
   :interfaces    shared-interfaces
   :input-objects {}
   :mutations     {:create_campaign      {:type        :Campaign
                                          :description "Creates a new campaign"
                                          :args        {:name {:type 'String}}
                                          :resolve     (fn [{:sky-deck/keys [datasource auth]} args _]
                                                         (sd.mutations/add-campaign datasource {:campaign-inputs
                                                                                                {:dungeon_master_id (:person-id auth)}}))}

                   :create_session       {:type        :Session
                                          :description "Creates a new campaign session."
                                          :args        {:campaign_id {:type 'String}}
                                          :resolve     (fn [{:sky-deck/keys [datasource auth]} args _]
                                                         (let [node (sd.node/node datasource (:campaign_id args))]
                                                           (sd.mutations/add-session
                                                             datasource
                                                             {:campaign-id (:campaign/id node)})))}

                   :create_battle        {:type        :Battle
                                          :description "Create a new battle"
                                          :args        {:session_id {:type 'String}}
                                          :resolve     (fn [{:sky-deck/keys [datasource auth]} args _]
                                                         (let [node (sd.node/node datasource (:session_id args))]
                                                           {:id "Battle"}))}

                   :create_npc_character {:type        :Character
                                          :description "Creates a new NPC Character"
                                          :args        {:name {:type 'String}}
                                          :resolve     (fn [{:sky-deck/keys [datasource auth]} args _]
                                                         (sd.mutations/add-npc-character datasource {:character-inputs args}))}}

   :queries       {:list_campaigns {:type    '(list :Campaign)
                                    :resolve (fn [{:sky-deck/keys [datasource auth]} args _]
                                               (sky-deck.queries/all-campaigns-by-dungeon-master datasource (:person-id auth)))}}})

(defn compile-dungeon-master-api
  []
  (schema/compile dungeon-master-api-schema))

(def authenticated-player-api-schema
  {:objects shared-objects})

(def anonymous-api-schema
  {:objects shared-objects
   :interfaces shared-interfaces
   :input-objects {}
   :mutations {:join_battle {}
               :create_account {}}
   :queries {}})
