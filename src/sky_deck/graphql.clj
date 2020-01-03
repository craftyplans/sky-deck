(ns sky-deck.graphql
  (:require
    [com.walmartlabs.lacinia.resolve :as lacinia-resolve]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]))

(def shared-interfaces
  {:Node {:fields {:id {:type '(non-null ID)}}}})

(def shared-objects
  {:Person {:implements [:Node]
            :fields {:id {:type '(non-null ID)}
                     :username {:type 'String}
                     :email {:type 'String}}}

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
                     :number {:type 'Number}
                     :state {:type 'String}
                     :session {:type :Session}
                     :campaign {:type :Campaign}
                     :participants {:type '(list :Character)}}}

   :Round {:implements [:Node]
           :fields {:id {:type '(non-null ID)}
                    :state {:type 'String}
                    :battle {:type :Battle}
                    :campaign {:type :Campaign}}}

   :Hand {:implements [:Node]
          :fields {:id {:type '(non-null ID)}
                   :round {:type :Round}
                   :character {:type :Character}
                   :battle {:type :Battle}
                   :state {:type 'String}}}

   :HandAction {:implements [:Node]
                :fields {:id {:type '(non-null ID)}
                         :hand {:type :Hand}
                         :action_type {:type :ActionType}}}

   :Campaign {:implements [:Node]
              :fields {:id {:type '(non-null ID)}
                       :number {:type 'Number}
                       :state {:type 'String}
                       :dungeon_master {:type :Person}
                       :players {:type '(list :Character)}}}

   :CampaignPlayer {:implements [:Node]
                    :fields {:id {:type '(non-null ID)}
                             :character {:type :Character}
                             :campaign {:type :Campaign}}}

   :Character {:implements [:Node]
               :fields {:id {:type '(non-null ID)}
                        :name {:type 'String}
                        :type {:type 'String}
                        :hit_point_max {:type 'Number}
                        :hit_point_current {:type 'Number}
                        :age {:type 'Number}
                        :agility {:type 'Number}
                        :strength {:type 'Number}
                        :mind {:type 'Number}
                        :soul {:type 'Number}
                        :skill_points {:type 'Number}
                        :reputation {:type 'Number}
                        :master_points {:type 'Number}
                        :divinity_points {:type 'Number}
                        :moments {:type 'Number}
                        :past_lives {:type 'Number}
                        :charges {:type 'Number}
                        :background {:type 'String}
                        :action_types {:type '(list :ActionType)}}}})

(def dungeon-master-api-schema
  {:objects shared-objects
   :interfaces shared-interfaces
   :input-objects {}
   :mutations {:createCampaign {}}
   :queries {}})

(def authenticated-player-api-schema
  {:objects shared-objects})

(def anonymous-api-schema
  {:objects shared-objects
   :interfaces shared-interfaces
   :input-objects {}
   :mutations {:joinBattle {}}
   :queries {}})
