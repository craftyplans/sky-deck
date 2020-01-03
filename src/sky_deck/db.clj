(ns sky-deck.db
  (:require [honeysql.core :as sql]
            [next.jdbc :as jdbc]
            [porsas.next]))

(defn execute-one-sql
  [ds sql-map]
  (let [sql-str (sql/format sql-map)]
    (next.jdbc/execute-one! ds sql-str)))

(defn execute-sql
  [ds sql-map]
  (next.jdbc/execute! ds (sql/format sql-map)))
