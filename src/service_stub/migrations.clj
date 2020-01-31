(ns service-stub.migrations
  " Database schema migration.
    inspired by 
    https://github.com/technomancy/syme/blob/master/src/syme/db.clj#L66-L119"
  (:require [clojure.java.jdbc :as sql]
            [service-stub.db :refer [db-conn]]
            [service-stub.conf :refer [load-res]])
  (:import (java.sql Timestamp)))

(defn load-sql [res]
  (load-res (str "sql/migrations/" res ".sql")))

(defn run-sql [conn res-name]
  (->>
   (load-sql res-name)
   (sql/db-do-commands conn)))

(defn logging [conn]
  (run-sql conn "logging"))

(defn run-and-record [conn migration]
  (migration conn)
  (sql/insert! conn "migrations" [:name :created_at]
               [(str (:name (meta migration)))
                (Timestamp. (System/currentTimeMillis))]))

(defn migrate [conn & migrations]
  (try
    (->>
     (sql/create-table-ddl "migrations"
                           [[:name :varchar "NOT NULL"]
                            [:created_at :timestamp
                             "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
     (sql/db-do-commands conn))
    (catch Exception _))
  (sql/with-db-transaction
    [db-conn conn]
    (let [has-run? (sql/query db-conn ["SELECT name FROM migrations"]
                              {:result-set-fn #(set (map :name %))})]
      (doseq [m migrations
              :when (not (has-run? (str (:name (meta m)))))]
        (run-and-record db-conn m)))))

(defn run-migration
  "update the DB to reflect all appropriate additions.
   Add new arguments to the migrate call as needed."
  [env]
  (migrate (db-conn env)
           #'logging))

(comment
  (in-ns 'service-stub.migrations)
  (run-migration :test)

  (def conn (db-conn :test))
  (clojure.java.jdbc/query conn ["select * from migrations"])
  ;;
  )