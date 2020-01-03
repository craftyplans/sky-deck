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
    [sky-deck.mutations :as sd.mutations])
  (:import (java.util Base64)))

(defn- encode-id
  [x]
  (.encodeToString (Base64/getEncoder) (.getBytes x)))

(defn- decode-id
  [x]
  (String. (.decode (Base64/getDecoder) x)))

(defn to-global-id
  [type id]
  (encode-id (str/join ":" [(name type) id])))

(defn from-global-id
  [global-id]
  (str/split (decode-id global-id) #":"))

(comment
  (sky-deck.graphql/to-global-id :Person #uuid"5072e832-2d86-11ea-b123-3f04c1c50b65"))

(def shared-interfaces
  {:Node {:fields {:id {:type '(non-null ID)}}}})

(def shared-objects
  {:Person {:implements [:Node]
            :fields {:id {:type '(non-null ID)}
                     :username {:type 'String}
                     :email {:type 'String}}}

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
             :fields {:id {:type '(non-null ID)}
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

   :Campaign {:implements [:Node]
              :fields {:id {:type '(non-null ID)
                            :resolve (fn [_ _ value]
                                       (to-global-id :Campaign (:campaign/id value)))}
                       :number {:type 'Int
                                :resolve (fn [_ _ value]
                                           (:campaign/number value))}
                       :state {:type 'String}
                       :dungeon_master {:type :Person}
                       :players {:type '(list :Character)}}}

   :Character {:implements [:Node]
               :fields {:id {:type '(non-null ID)}
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
   :mutations     {:create_campaign {:type        :Campaign
                                     :description ""
                                     :args        {:name {:type 'String}}
                                     :resolve     (fn [{:sky-deck/keys [datasource auth]} args _]
                                                    (let [results (jdbc/execute-one!
                                                                    datasource
                                                                    (sql/format (sd.mutations/generate-campaign {:campaign-inputs
                                                                                                                 {:dungeon_master_id (:person-id auth)}})))]
                                                      (clojure.pprint/pprint [:results results])
                                                      results
                                                      ))}}

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
