(ns service-stub.conf
  (:require
   [clojure.java.io :as io]))

(defn load-res [res]
  (-> res
      io/resource
      slurp))