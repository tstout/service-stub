(ns service-stub.main
  (:require
   [clojure.edn :as edn]
   [taoensso.timbre :as log]
   [service-stub.conf :refer [memoized-res]]))

(defn wrap-env
  "Add environment information into the ring request.
  The environment should be one of :dev :test or :prod"
  [handler env]
  (fn [request]
    (handler (assoc request :env env))))

(defn read-body
  "In Ring, the http body is actualy a java InputStream.
   This function consumes the InputStream and converts the resulting 
   string into a clojure persistent data structure."
  [body]
  (->
   body
   slurp
   edn/read-string))

(defn mk-response
  "create a ring response map"
  ([body]
   (mk-response body 200))

  ([body status]
   (mk-response body status {"Content-Type" "text/html"}))

  ([body status headers]
   {:status  status
    :headers headers
    :body    body}))

(defmulti router
  (fn [request]
    (let [{:keys [uri request-method]} request]
      (log/infof "URI is %s" uri)
      (->
       uri
       {"/hello" ({:get :hello} request-method)
        "/receipt-services/receipt" ({:get :receipt} request-method)}))))

(defmethod router :hello [_]
  (mk-response "Hello World"))

(defmethod router :receipt [request]
  (log/infof "responding with stub receipt response for query %s" (request :query-string))
  (mk-response (memoized-res "json/receipt.json")))

(defmethod router :default [request]
  (let [{:keys [uri]} request]
    (log/infof "Unrecognized URI %s" uri)
    (mk-response (str "Unknown URI " uri) 400)))

(defn handler [request]
  (let [{:keys [uri]} request]
    (log/infof "Processing uri %s" uri)
    (router request)))

(comment
  ;; REPL experiments
  (router {:uri "/hello" :request-method :get})

  (router {:uri "/receipt-services/receipt" :request-method :get})
  ;;
  )