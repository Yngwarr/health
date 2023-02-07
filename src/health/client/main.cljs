(ns health.client.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dumdom.core :as d]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(declare update-view)

(def search-query (atom ""))
(def now-editing (atom nil))
(def patients-info (atom []))

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

(defn gather-details []
  {:fullname (-> js/document (.getElementById "fullname") .-value)
   :gender (-> js/document (.getElementById "gender") .-value)
   :birthdate (-> js/document (.getElementById "birthdate") .-value)
   :address (-> js/document (.getElementById "address") .-value)
   :insurancenum (-> js/document (.getElementById "insurancenum") .-value)})

(defn set-input
  ([id] (set-input id ""))
  ([id default] (set! (-> js/document (.getElementById id) .-value) default)))

(defn clear-details []
  (doseq [id ["fullname" "birthdate" "address" "insurancenum"]]
    (set-input id))
  (set-input "gender" "female"))

(defn add-patient []
  (clear-details)
  (set-details-visibility true))

(defn submit-details []
  ; TODO handle edit
  (let [details (gather-details)]
    (prn details)
    ; TODO handle fail
    (go (let [result (<! (http/post "patient" {:transit-params details}))]
          (if (= (:status result) 200)
            (do (set-details-visibility false)
                (update-view)
                (reset! now-editing nil)))))))

(d/defcomponent Controls [props]
  [:div.row.controls
   [:input {:type "text" :id "search-bar" :placeholder "Find something..."}]
   [:button {:on-click (fn [e] (search))} "Search"]
   [:button {:on-click (fn [e] (add-patient))} "Add a patient"]])

(defn delete-patient [id]
  (go (let [response (<! (http/delete (str "patient/" id)))]
        (update-view))))

(defn fill-details [patient]
  (set-input "fullname" (:patients/fullname patient))
  (set-input "gender" (:patients/gender patient))
  (set-input "birthdate" (:patients/birthdate patient))
  (set-input "address" (:patients/address patient))
  (set-input "insurancenum" (:patients/insurancenum patient)))

(defn edit-patient [id]
  (let [patient (first (filter #(= (:patients/id %) id) @patients-info))]
    (prn patient)
    (reset! now-editing id)
    (fill-details patient)
    (set-details-visibility true)))

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
    [:label "Insurance #:" [:input {:type "number" :id "insurancenum" :placeholder "01101111010101101"}]]
    [:div.row
     [:button {:on-click (fn [e] (submit-details))} "Submit"]
     [:button {:on-click (fn [e] (set-details-visibility false))} "Cancel"]]
    ]])

(d/defcomponent Page [patients]
  [:main
   (Controls [])
   (Table patients)
   (Modal [])])

(defn render [patients]
  (d/render (Page patients) (-> js/document .-body)))

(defn render-error [response]
  (d/render [:main response] (-> js/document .-body)))

; TODO improve error handling
(defn update-view []
  (go (let [opts {:query-params (if (empty? @search-query) {} {"q" @search-query})}
            response (<! (http/get "patients" opts))]
        (prn (:body response))
        (if (= (:status response) 200)
          (do
            ; TODO cast to hash-map
            (reset! patients-info (:body response))
            (render @patients-info))
          (render-error (:body response))))))

(update-view)
