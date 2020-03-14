(ns sky-deck.resolvers
  (:require [sky-deck.queries :as sd.queries]
            [sky-deck.mutations :as sd.mutations]
            [for-humans.pipeline :as pipeline]
            [cambium.core :as log]
            [next.jdbc :as jdbc]))

(def default-character-stats
  {:character-inputs {:hit_point_max     10
                      :hit_point_current 10
                      :mind              1
                      :soul              1
                      :agility           1
                      :age               1
                      :past_lives        1
                      :strength          1
                      :skill_points      0
                      :reputation        0
                      :moments           0
                      :charges           0}})


(defn resolve-battle
  [ctx attrs _value]
  (with-open [connection (jdbc/get-connection (jdbc/get-datasource (:sky-deck/db
                                                                    ctx)))]
    (sd.queries/battle-by-number connection (:number attrs))))

(defn find-campaign-by-battle
  [ctx _attrs value]
  (with-open [connection (jdbc/get-connection (jdbc/get-datasource (:sky-deck/db
                                                                    ctx)))]
    (sd.queries/campaign-by-battle connection value)))

(defn find-participants
  [ctx attrs value]
  (with-open [conn (jdbc/get-connection (jdbc/get-datasource (:sky-deck/db
                                                              ctx)))]
    (sd.queries/participants conn value)))

;; find battle,
;; create new default character with random name
;; associates that character with the current battle
;; also creates battle-participant
;; and campaign-player
;; returns character to client.

(defn lookup-battle [ctx attrs _value])

(defn anonymously-join-battle
  [ctx attrs value]
  (let [data-source (jdbc/get-datasource (:sky-deck/db ctx))]
    (with-open [connection (jdbc/get-connection data-source)]
      (let [battle (sd.queries/battle-by-number connection (:number attrs))
            campaign-id (:battle/campaign_id battle)
            character-id (sd.mutations/new-id)
            tx-data {:character       (sd.mutations/generate-anonymous-character
                                       (assoc default-character-stats
                                              :new-id
                                              character-id))
                     :participant     (sd.mutations/generate-battle-participant
                                       {:battle-id    (:battle/id battle)
                                        :character-id character-id})
                     :campaign-player (sd.mutations/generate-campaign-player
                                       {:character-id character-id
                                        :campaign-id  campaign-id})}
            new-objects (sd.mutations/transact-map ctx tx-data)]
        (:character new-objects)))))

(defn claim-anonymous-character [ctx attrs value])
