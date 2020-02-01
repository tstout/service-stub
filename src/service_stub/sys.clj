(ns service-stub.sys
  (:require [service-stub.db :as db]
            [service-stub.logging :refer [init-logging]]
            [service-stub.migrations :refer [run-migration]]))

(def server (db/mk-h2-server))

(defn start
  "Start the components comprising the service"
  [env]
  (server :start)
  (run-migration env)
  (init-logging env))

(defn stop "Stop the system components" []
  (server :stop))