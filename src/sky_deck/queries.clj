(ns sky-deck.queries
  (:require [honeysql.core :as sql]
            [next.jdbc :as jdbc]
            [porsas.next]
            [sky-deck.db :as db]))

(defn generate-by-username
  [username]
  {:select [:*]
   :from [:person]
   :where [:= :person.username username]})

(defn generate-by-id
  [id]
  {:select [:*]
   :from [:person]
   :where [:= :person/id id]})

(defn person-by-username
  [ds username]
  (db/execute-one-sql ds (generate-by-username username)))

(comment

  ;; 94fceaea-93be-494c-aa77-a9b089065d46
  #:person{:id #uuid"94fceaea-93be-494c-aa77-a9b089065d46",
           :created_at #inst"2020-01-02T19:43:51.752104000-00:00",
           :updated_at #inst"2020-01-02T19:43:51.752104000-00:00",
           :deleted_at nil,
           :username "ivan",
           :email "iwillig@gmail.com",
           :enabled true}

  )

(defn person-by-id
  [ds id]
  (db/execute-one-sql ds (generate-by-id id)))


(defn campaign-by-number
  [ds number]
  (db/execute-one-sql ds {:select [:*]
                          :from       [:campaign]
                          :where      [:= :campaign/number number]}))

(defn all-campaigns-by-dungeon-master
  [ds dungeon-master-id]
  (db/execute-sql ds
                  {:select [:*]
                   :from       [:campaign]
                   :where      [:= :campaign/dungeon_master_id dungeon-master-id]}))

(defn campaign-players
  [ds campaign-id]
  (db/execute-sql ds {:select [:*]}))

