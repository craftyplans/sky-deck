(ns sky-deck.db
  (:require [honeysql.core :as sql]
            [next.jdbc :as jdbc]
            [integrant.core :as ig]
            [porsas.next]))

(defn execute-one-sql
  [ds sql-map]
  (let [sql-str (sql/format sql-map)]
    (next.jdbc/execute-one! ds sql-str)))

(defn execute-sql
  [ds sql-map]
  (next.jdbc/execute! ds (sql/format sql-map)))

(defmethod ig/init-key :sky-deck/db
  [_ options]
  options)

(defmethod ig/init-key :sky-deck/connection-uri
  [_ options]
  options)
