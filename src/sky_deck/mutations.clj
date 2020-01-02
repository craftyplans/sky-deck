(ns sky-deck.mutations
  (:require [clojure.spec.alpha :as s]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [medley.core :as m]
            [honeysql-postgres.format]
            [honeysql-postgres.helpers])
  (:import (java.util UUID)))

(defn new-id
  []
  (UUID/randomUUID))

(s/def ::id uuid?)

(s/def ::text (s/and string? (complement str/blank?)))
(s/def ::pos-number? number?)

(s/def ::username ::text)
(s/def ::email ::text)
(s/def ::password ::text)

(s/def ::person-inputs
  (s/keys :req-un [::username
                   ::email
                   ::password]))

(s/def ::person-args
  (s/keys :req-un [::person-inputs]
          :opt-un [::new-id]))

(defn generate-person
  [{:keys [new-id person-inputs]
    :as   _person-args}]
  {:insert-into :person
   :values      [(m/assoc-some
                   (update person-inputs :password hashers/derive)
                   :id new-id)]
   :returning   [:*]})

(s/fdef generate-person
        :args (s/cat :person-args ::person-args)
        :ret map?)

(s/def ::description ::text)
(s/def ::dungeon_master_id ::id)
(s/def ::campaign-inputs (s/keys :req-un [::dungeon_master_id ::description ::name]))

(s/def ::campaign-args
  (s/keys :req-un [::campaign-inputs]
          :opt-un [::new-id]))

(defn generate-campaign
  [{:keys [new-id campaign-inputs]
    :as   _campaign-args}]
  {:insert-into [:campaign]
   :values [(m/assoc-some campaign-inputs :id new-id)]
   :returning [:*]})

(s/fdef generate-campaign
        :args (s/cat :campaign-args ::campaign-args)
        :ret map?)

(s/def ::session-args (s/keys :req-un [::campaign-id]
                              :opt-un [::new-id]))

(defn generate-session
  [{:keys [new-id campaign-id]
    :as   _session-args}]
  {:insert-into [:session]
   :values [(m/assoc-some {:campaign_id campaign-id} :id new-id)]
   :returning [:*]})

(s/fdef generate-session
        :args (s/cat :session-args ::session-args)
        :ret map?)

(s/def ::name string?)
(s/def ::background string?)
(s/def ::hit-point-max number?)
(s/def ::hit-point-current number?)
(s/def ::agility ::pos-number?)
(s/def ::strength ::pos-number?)
(s/def ::mind ::pos-number?)
(s/def ::soul ::pos-number?)
(s/def ::skill-points ::pos-number?)
(s/def ::reputation ::pos-number?)
(s/def ::master-points ::pos-number?)
(s/def ::divinity-points ::pos-number?)
(s/def ::moments ::pos-number?)
(s/def ::past-lives ::pos-number?)
(s/def ::charges ::pos-number?)
(s/def ::age ::pos-number?)
(s/def ::height ::pos-number?)
(s/def ::clothes string?)

(s/def ::character-inputs
  (s/keys :req-un [::name
                   ::deck
                   ::hit_point_max
                   ::hit_point_current
                   ::agility
                   ::strength
                   ::mind
                   ::soul
                   ::skill-points
                   ::reputation
                   ::master_points
                   ::divinity_points
                   ::moments
                   ::past_lives
                   ::charges
                   ::age
                   ::clothes
                   ::height
                   ::background]))

(s/def ::character-args
  (s/keys :req-un [::new-id
                   ::person-id
                   ::campaign-id
                   ::character-inputs]))

(defn generate-character
  [{:keys [new-id person-id campaign-id character-inputs]}]
  {:insert-into [:character]
   :values [(m/assoc-some character-inputs
                          :id new-id
                          :person_id person-id
                          :campaign_id campaign-id)]
   :returning [:*]})

(s/fdef generate-character
        :args (s/cat :character-args ::character-args)
        :ret map?)

(defn generate-action-type
  [{:keys [new-id action-name action-type]}]
  {:insert-into [:action-type]
   :values [(m/assoc-some {} :id new-id)]
   :returning [:*]})

(defn generate-battle
  [{:keys [new-id campaign-id]}])

(defn generate-hand
  [{:keys [new-id]}])
