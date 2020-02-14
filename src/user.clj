(ns user
  (:require [iapetos.collector.jvm :as jvm]
            [iapetos.collector.ring :as ring]
            [iapetos.core :as prometheus]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :as log]
            [service-stub.main :refer [handler wrap-env]]
            [service-stub.sys :as sys]))

(defonce registry
  (-> (prometheus/collector-registry)
      (jvm/initialize)
      (ring/initialize)))

(defn start
  "Start the web app"
  []
  (sys/start :dev)
  (log/info "Starting server on port 4000")
  (run-jetty (-> #'handler
                 (wrap-env :dev)
                 (wrap-reload)
                 (ring/wrap-metrics registry))
             {:join? false
              :port 4000}))