(ns for-humans.migrations
  (:require [ragtime.jdbc :as rt.jdbc]
            [ragtime.protocols :as rt.protocols]
            [ragtime.repl :as rt.repl]
            [next.jdbc :as n.jdbc]
            [honeysql.core :as hs.sql]
            [integrant.core :as ig]
            [honeysql.helpers :as hs.helpers]
            [honeysql-postgres.helpers :as hs-pg.helpers]
            [honeysql-postgres.format :as hs-pg.format]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]))
;
;(defn- run-migration
;  [db migration-data]
;  (with-open [connection (n.jdbc/get-connection (get-in db [:db-spec :connection-uri]))]
;    (let [migration-sql-string (if (map? migration-data)
;                                 (hs.sql/format migration-data)
;                                 migration-data)]
;      (tap> {:for-humans/migration-sql migration-sql-string})
;      (n.jdbc/execute! connection migration-sql-string))))
;
;(defrecord DataSqlMigration [id up down transactions]
;  rt.protocols/Migration
;  (id [_self] (name id))
;  (run-up! [_self db]
;    (tap> {:for-humans/run-up {:id id :up up}})
;    (run-migration db up))
;  (run-down! [_self db]
;    (tap> {:for-humans/run-down {:id id :down down}})
;    (run-migration db down)))
;
;(defn load-edn-migrations
;  [path]
;  (let [migration-data (edn/read-string
;                         {:readers {'sql-call (fn [values]
;                                                (apply hs.sql/call values))}}
;                         (slurp (io/resource path)))]
;    (mapv map->DataSqlMigration migration-data)))
;
;(def config {:datastore (rt.jdbc/sql-database {:connection-uri
;                                               "jdbc:postgresql://localhost/daily_meal"})
;             :migrations (load-edn-migrations "migrations.edn")})
;
;(defmethod ig/init-key :for-humans/ragtime.migrations
;  [_ {:keys [connection-uri migration-edn-file] :as config}]
;  (rt.repl/migrate {:datastore (rt.jdbc/sql-database {:connection-uri connection-uri})
;                    :migrations (load-edn-migrations migration-edn-file)})
;  config)
;
;(defn migrate-all
;  []
;  (rt.repl/migrate config))
;
;(defn rollback
;  []
;  (rt.repl/rollback config))

(defmethod ig/init-key :for-humans/migrations
  [_ options]
  (let [realized-config {:datastore  (rt.jdbc/sql-database {:connection-uri (:connection-uri options)})
                         :migrations (rt.jdbc/load-resources (:migrations options))}]
    (rt.repl/migrate realized-config)
    realized-config))

(comment
  (for-humans.migrations/migrate-all)
  (for-humans.migrations/rollback)
  (for-humans.migrations/load-edn-migrations "migrations.edn"))
