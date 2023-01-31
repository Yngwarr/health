(ns health.client.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dumdom.core :as d]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(d/defcomponent Controls [props]
  [:div.row
   [:input {:type "text" :id "search-bar" :placeholder "Find something..."}]
   [:button "Search"]
   [:button "Add a patient"]])

(d/defcomponent Actions [id]
  [:div
   [:button.action.material-symbols-outlined "edit"]
   [:button.action.material-symbols-outlined "delete"]])

(d/defcomponent Table [patients]
  [:table {:id "patients"}
   [:tr [:th "ID"]
    [:th "Full Name"]
    [:th "Gender"]
    [:th "Birthdate"]
    [:th "Address"]
    [:th "Insurance #"]
    [:th "Actions"]]
   ; TODO prettify code below
   (for [p patients]
     (let [id (:patients/id p)]
       [:tr [:td id]
        [:td (:patients/fullname p)]
        [:td (:patients/gender p)]
        ; TODO convert date to a more readable foramt
        [:td (:patients/birthdate p)]
        [:td (:patients/address p)]
        [:td (:patients/insurancenum p)]
        [:td (Actions id)]]))])

(d/defcomponent Page [patients]
  [:main (Controls []) (Table patients)])

(defn render [patients]
  (d/render (Page patients) (js/document.getElementById "body")))

;(defn onload []
  ;(println "I've loaded!"))

;(-> js/document .-body (.addEventListener "load" onload))

; TODO handle error statuses
(go (let [response (<! (http/get "http://localhost:8080/patients"))]
      (println (:body response))
      (render (:body response))))
