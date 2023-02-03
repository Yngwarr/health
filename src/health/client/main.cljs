(ns health.client.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dumdom.core :as d]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(declare update-view)

(def host "http://localhost:8080/")
(def search-query (atom ""))

(defn search []
  (reset! search-query (-> js/document (.getElementById "search-bar") .-value))
  (update-view))

(defn get-modal []
  (-> js/document (.getElementById "modal-background")))

(defn set-details-visibility [visible]
  (let [modal (get-modal)]
    (if visible
      (-> modal .-classList (.remove "hidden"))
      (-> modal .-classList (.add "hidden")))))

(defn add-patient []
  ; TODO clear details
  (set-details-visibility true))

(d/defcomponent Controls [props]
  [:div.row.controls
   [:input {:type "text" :id "search-bar" :placeholder "Find something..."}]
   [:button {:on-click (fn [e] (search))} "Search"]
   [:button {:on-click (fn [e] (add-patient))} "Add a patient"]])

(defn delete-patient [id]
  (go (let [response (<! (http/delete (str "patient/" id)))]
        (update-view))))

(defn edit-patient [id]
  ; TODO fill in the info
  (set-details-visibility true))

(d/defcomponent Actions [id]
  [:div
   [:button.action.material-symbols-outlined
    {:on-click (fn [e] (edit-patient id))} "edit"]
   [:button.action.material-symbols-outlined
    {:on-click (fn [e] (delete-patient id))} "delete"]])

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

(d/defcomponent Modal [props]
  [:div.hidden {:id "modal-background"}
   [:div.modal {:id "details"}
    [:label "Full name:" [:input {:type "text" :id "fullname" :placeholder "John Doe"}]]
    [:label "Gender:" [:select {:id "gender"}
                       [:option {:value "female"} "Female"]
                       [:option {:value "male"} "Male"]
                       [:option {:value "other"} "Other"]]]
    [:label "Birthdate:" [:input {:type "date" :id "birthdate" :placeholder "1981-12-31"}]]
    [:label "Address:" [:input {:type "text" :id "address" :placeholder "Finland"}]]
    [:div.row
     [:button "Submit"]
     [:button {:on-click (fn [e] (set-details-visibility false))} "Cancel"]]
    ]])

(d/defcomponent Page [patients]
  [:main
   (Controls [])
   (Table patients)
   (Modal [])])

(defn render [patients]
  (d/render (Page patients) (-> js/document .-body)))

;(defn onload [] (println "I've loaded!"))
;(-> js/document .-body (.addEventListener "load" onload))

; TODO handle error statuses
(defn update-view []
  ; TODO send @search-query as a parameter
  (go
    (let [opts {:query-params (if (empty? @search-query) {} {"q" @search-query})}
          response (<! (http/get (str host "patients") opts))]
      (println (:body response))
      (render (:body response)))))

(update-view)
