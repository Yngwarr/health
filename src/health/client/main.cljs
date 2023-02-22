(ns health.client.main
  (:require
    [reagent.dom]
    [health.client.subs]
    [health.client.views]
    [health.client.events]
    [re-frame.core :refer [dispatch-sync]]
    [health.client.views :refer [client-app]]
    [health.common.validation :as v]))

(enable-console-print!)

(dispatch-sync [:init-db])

(defn render []
  (reagent.dom/render
    [client-app]
    (-> js/document (.getElementsByTagName "main") first)))

(render)
