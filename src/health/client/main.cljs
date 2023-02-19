(ns health.client.main
  (:require
    [reagent.dom]
    [health.client.views :refer [client-app]]
    [health.common.validation :as v]))

(defn render []
  (reagent.dom/render [client-app]
                      (-> js/document (.getElementsByTagName "main") first)))

(render)
