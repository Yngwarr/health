(ns health.client.main
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.dom]
            [health.client.subs]
            [health.client.views]
            [health.client.events]
            [re-frame.core :refer [dispatch-sync dispatch]]
            [secretary.core :as secretary]
            [health.client.views :refer [client-app]]
            [health.common.validation :as v])
  (:import [goog History]
           [goog.history EventType]))

(enable-console-print!)

(dispatch-sync [:init-db])

(defroute "/" [] (dispatch [:search ""]))
(defroute "/s/:query" [query] (dispatch [:search query]))
(defroute "/p/:id" [id] (dispatch [:show-patient (js/parseInt id 10)]))

(defonce history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [^js/goog.History.Event event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn render []
  (reagent.dom/render
    [client-app]
    (-> js/document (.getElementsByTagName "main") first)))

(render)
