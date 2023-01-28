(ns health.client.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dumdom.core :as d]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(d/defcomponent Controls [props]
  [:div {:class "row"}
   [:input {:type "text" :id "search-bar" :placeholder "Find something..."}]
   [:button "Search"]
   [:button "Add a patient"]])

(d/defcomponent Table [props]
  [:table
   [:th [:td "ID"]
    [:td "Full Name"]
    [:td "Gender"]
    [:td "Birthdate"]
    [:td "Address"]
    [:td "Insurance #"]]])

(d/defcomponent Page [props]
  [:main (Controls []) (Table [])])

(defn render []
  (d/render (Page []) (js/document.getElementById "body")))

;(defn onload []
  ;(println "I've loaded!"))

;(-> js/document .-body (.addEventListener "load" onload))

(go (let [response (<! (http/get "http://localhost:8080/patients"))]
      (println response)))
