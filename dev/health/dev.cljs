(ns ^:figwheel-hooks health.dev
  (:require
    [re-frame.core :as rf]
    [health.client.main :as main]))

(defn ^:after-load render []
  (rf/clear-subscription-cache!)
  (main/render))
