(ns sky-deck.mutations
  (:require [clojure.spec.alpha :as s]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [medley.core :as m]
            [next.jdbc :as jdbc]
            [honeysql-postgres.format]
            [honeysql-postgres.helpers]
            [honeysql.core :as sql]
            [sky-deck.db :as db])
  (:import (java.util UUID)))

(defn transact-map
  [{:keys [:sky-deck/db]} tx-data]
  ;; Just do this for now, it will not scale.
  (jdbc/with-transaction [tx db]
                         (->> (for [[key insert-sql] tx-data]
                                [key (db/execute-one-sql tx insert-sql)])
                              (into {}))))

(defn new-id [] (UUID/randomUUID))

(s/def ::id uuid?)

(s/def ::text (s/and string? (complement str/blank?)))
(s/def ::int (s/int-in -100000 100000))
(s/def ::pos-number? (s/and pos? ::int))

(s/def ::username ::text)
(s/def ::email ::text)
(s/def ::password ::text)

(s/def ::person-inputs
  (s/keys :req-un [::username
                   ::email
                   ::password]))

(s/def ::person-args (s/keys :req-un [::person-inputs] :opt-un [::new-id]))

(defn generate-person
  [{:keys [new-id person-inputs]
    :as   _person-args}]
  {:insert-into :person
   :values      [(m/assoc-some (update person-inputs :password hashers/derive)
                               :id
                               new-id)]
   :returning   [:*]})

(s/fdef generate-person
  :args (s/cat :person-args ::person-args)
  :ret map?)

(s/def ::description ::text)
(s/def ::dungeon_master_id ::id)
(s/def ::campaign-id ::id)

(s/def ::campaign-inputs (s/keys :req-un [::dungeon_master_id]))

(s/def ::campaign-args (s/keys :req-un [::campaign-inputs] :opt-un [::new-id]))

(defn generate-campaign
  [{:keys [new-id campaign-inputs]
    :as   _campaign-args}]
  {:insert-into :campaign
   :values      [(m/assoc-some campaign-inputs :id new-id)]
   :returning   [:*]})

(s/fdef generate-campaign
  :args (s/cat :campaign-args ::campaign-args)
  :ret map?)

(defn add-campaign [ds opts] (db/execute-one-sql ds (generate-campaign opts)))

(s/def ::session-args (s/keys :req-un [::campaign-id] :opt-un [::new-id]))

(defn generate-session
  [{:keys [new-id campaign-id]
    :as   _session-args}]
  {:insert-into :session
   :values      [(m/assoc-some {:campaign_id campaign-id} :id new-id)]
   :returning   [:*]})

(s/fdef generate-session
  :args (s/cat :session-args ::session-args)
  :ret map?)

(defn add-session [ds opts] (db/execute-one-sql ds (generate-session opts)))

(s/def ::name string?)
(s/def ::background string?)

(s/def ::hit_point_max ::int)
(s/def ::hit_point_current ::int)
(s/def ::agility ::int)
(s/def ::strength ::int)
(s/def ::mind ::int)
(s/def ::soul ::int)
(s/def ::skill_points ::int)
(s/def ::reputation ::int)
(s/def ::master_points ::int)
(s/def ::divinity_points ::int)
(s/def ::moments ::int)
(s/def ::past_lives ::int)
(s/def ::charges ::int)
(s/def ::age ::int)
(s/def ::type #{"player" "npc" "anonymous"})

(s/def ::character-inputs
  (s/keys :req-un [::hit_point_max
                   ::hit_point_current
                   ::agility
                   ::strength
                   ::mind
                   ::soul
                   ::skill_points
                   ::reputation
                   ::moments
                   ::past_lives
                   ::charges]
          :opt-un [::type ::background]))

(s/def ::character-args
  (s/keys :req-un [::character-inputs] :opt-un [::new-id]))

(defn generate-character
  [{:keys [new-id character-inputs]}]
  {:insert-into :character
   :values      [(m/assoc-some character-inputs
                               :type (sql/call :cast
                                               (:type character-inputs "player")
                                               :character_type)
                               :id new-id)]
   :returning   [:*]})

(s/fdef generate-character
  :args (s/cat :character-args ::character-args)
  :ret map?)

(defn generate-anonymous-character
  [opts]
  (generate-character (assoc-in opts [:character-inputs :type] "anonymous")))


(defn add-anonymous-character
  [data-source opts]
  (db/execute-one-sql data-source (generate-character opts)))

(defn add-npc-character
  [ds opts]
  (db/execute-one-sql ds (generate-character opts)))

(s/def ::action-type-inputs (s/keys :req-un [::name ::slug]))

(s/def ::action-type-args
  (s/keys :req-un [::action-type-inputs] :opt-un [::new-id]))

(defn generate-action-type
  [{:keys [new-id action-type-inputs]}]
  {:insert-into :action_type
   :values      [(m/assoc-some action-type-inputs :id new-id)]
   :returning   [:*]})

(s/fdef generate-action-type
  :args (s/cat ::action-type ::action-type-args))

(defn generate-battle
  [{:keys [new-id campaign-id session-id initiated-by-id]}]
  {:insert-into :battle
   :values      [(m/assoc-some {:campaign_id campaign-id
                                :session_id  session-id}
                               :id new-id
                               :initiated_by_id initiated-by-id)]
   :returning   [:*]})

(defn add-battle [ds opts] (db/execute-one-sql ds (generate-battle opts)))

(defn generate-round
  [{:keys [new-id battle-id campaign-id]}]
  {:insert-into :round
   :values      [(m/assoc-some {:battle_id battle-id} :id new-id)]
   :returning   [:*]})

;+--------------+------------+----------------------------------------+
;| Column       | Type       | Modifiers                              |
;|--------------+------------+----------------------------------------|
;| id           | uuid       |  not null default uuid_generate_v1mc() |
;| round_id     | uuid       |                                        |
;| character_id | uuid       |                                        |
;| battle_id    | uuid       |                                        |
;| state        | hand_state |  default 'open'::hand_state            |
;+--------------+------------+----------------------------------------+

(s/def ::round-id ::id)
(s/def ::character-id ::id)
(s/def ::battle-id ::id)
(s/def ::state #{"open" "closed"})

(s/def ::hand-args
  (s/keys :req-un [::round-id ::character-id ::battle-id]
          :opt-un [::state ::new-id]))

(defn generate-hand
  [{:keys [new-id round-id character-id battle-id state]}]
  {:insert-into :hand
   :values      [(m/assoc-some {:round_id     round-id
                                :character_id character-id
                                :battle_id    battle-id}
                               :id new-id
                               :state state)]
   :returning   [:*]})

(s/fdef generate-hand
  :args (s/cat :hand-args ::hand-args)
  :ret map?)

(defn generate-battle-participant
  [{:keys [battle-id character-id]}]
  {:insert-into :battle_participant
   :values      [{:battle_id    battle-id
                  :character_id character-id}]
   :returning   [:*]})

(defn generate-campaign-player
  [{:keys [character-id campaign-id]}]
  {:insert-into :campaign_player
   :values      [{:character_id character-id
                  :campaign_id  campaign-id}]
   :returning   [:*]})
