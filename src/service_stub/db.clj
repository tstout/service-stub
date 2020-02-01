(ns service-stub.db
  (:require [clojure.java.jdbc :as jdbc])
  (:import (java.net InetAddress)
           (org.h2.tools Server)
           (org.h2.jdbcx JdbcConnectionPool)))

(defn host-name []
  (.. InetAddress getLocalHost getHostName))

(def h2-server
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :subname     (format "tcp://%s/~/.service-stub/db/service-stub;jmx=true" (host-name))
   :user        "sa"
   :password    ""})

(def h2-mem
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :subname     "mem:service-stub;DB_CLOSE_DELAY=-1"
   :user        "sa"
   :password    ""})

(defn mk-h2-server
  "Create an H2 server. Returns a function which accepts the operations
  :start and :stop"
  []
  (let
   [server (->
            (into-array String ["-tcpAllowOthers"])
            Server/createTcpServer)
    server-ops {:start (fn [] (.start server))
                :stop  (fn [] (.stop server))}]
    (fn [operation & args] (-> (server-ops operation) (apply args)))))

(defn mk-h2-pool
  "Create a basic H2 connection pool (supplied by H2)
   Nothing fancy, but more than adequate for this app."
  [db-spec]
  {:datasource
   (let [{:keys [subname user password]} db-spec]
     (JdbcConnectionPool/create
      (format "jdbc:h2:%s" subname)
      user
      password))})

(defmulti db-conn "Create an env-specific DB connection" identity)

(defmethod db-conn :test [_]
  (mk-h2-pool h2-mem))

(defmethod db-conn :prod [_]
  (mk-h2-pool h2-server))

(defmethod db-conn :dev [_]
  (mk-h2-pool h2-server))

(def mk-conn (memoize db-conn))

(comment
  (in-ns 'service-stub.db)
  (def server (mk-h2-server))

  (server :start)
  ;;(run-migration)
  (jdbc/query (mk-conn :dev) ["select * from migrations"])
  ;;
  )