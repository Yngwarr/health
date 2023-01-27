(ns health.client.main
  (:require [dumdom.core :as d]))

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

(println "Sup from main!")
