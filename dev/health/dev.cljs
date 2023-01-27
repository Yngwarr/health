(ns ^:figwheel-hooks health.dev
  (:require [health.client.main :as main]))

(defn ^:after-load render []
  (main/render))
