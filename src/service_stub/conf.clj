(ns service-stub.conf
  (:require
   [clojure.java.io :as io]
   [taoensso.timbre :as log]))

(defn load-res [res]
  (log/infof "loading resource %s" res)
  (-> res
      io/resource
      slurp))

(def memoized-res
  (memoize load-res))

(comment
  (memoized-res "json/receipt.json")
  ;;
  )