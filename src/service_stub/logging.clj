(ns service-stub.logging
  (:require [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]
            [service-stub.db :refer [db-conn]])
  (:import (java.sql Timestamp)
           (java.util Date)))

(defn log-message [config data]
  (let [{:keys [instant level ?ns-str msg_]} data
        entry
        {:instant   (Timestamp. (.getTime ^Date instant))
         :level     (str level)
         :namespace (str ?ns-str)
         :msg       (str (force msg_))}]

    (jdbc/with-db-connection
      [conn config]
      (jdbc/insert! conn :log entry))))

(defn h2-appender [db]
  {:enabled?   true
   :async?     true
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (fn [data] (log-message db data))})

(defn config-logging [db]
  (log/set-level! :debug)
  (log/merge-config! {:appenders {:h2 (h2-appender db)}
                      :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss.SS"}})
  (log/info "Logging Initialized"))

(defmulti init-logging identity)

(defmethod init-logging :test [env]
  (config-logging (db-conn :test)))

(defmethod init-logging :dev [env]
  (config-logging (db-conn :dev)))

(defmethod init-logging :prod [env]
  (config-logging (db-conn :prod)))