(ns health.client.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs.core.match :refer-macros [match]]
            [dumdom.core :as d]
            [cljs-http.client :as http]
            [health.common.validation :as v]))

(declare update-view)

(def search-query (atom ""))
(def now-editing (atom nil))
(def patients-info (atom []))

(defn get-element [id]
  (-> js/document (.getElementById id)))

(defn search []
  (reset! search-query (.-value (get-element "search-bar")))
  (update-view))

(defn clear-details-errors []
  (doseq [input (-> js/document (.querySelectorAll "#details input, #details select"))]
    (-> input .-classList (.remove "error"))))

(defn set-details-visibility [visible]
  (let [modal (get-element "modal-background")]
    (if visible
      (-> modal .-classList (.remove "hidden"))
      (do
        (-> modal .-classList (.add "hidden"))
        (clear-details-errors)
        (reset! now-editing nil)))))

(defn gather-details []
  {:fullname (.-value (get-element "fullname"))
   :gender (.-value (get-element "gender"))
   :birthdate (.-value (get-element "birthdate"))
   :address (.-value (get-element "address"))
   :insurancenum (.-value (get-element "insurancenum"))})

(defn set-input
  ([id] (set-input id ""))
  ([id default] (set! (.-value (get-element id)) default)))

(defn clear-details []
  (doseq [id ["fullname" "birthdate" "address" "insurancenum"]]
    (set-input id))
  (set-input "gender" "female"))

(defn add-patient []
  (clear-details)
  (set-details-visibility true))

(defn submit-request [details]
  (if (nil? @now-editing)
    (http/post "patient" {:transit-params details})
    (http/patch (str "patient/" @now-editing) {:transit-params details})))

(defn update-error [id error]
  (let [input (get-element (name id))
        error-text (-> js/document (.querySelector (str "#" (name id) " ~ .error-text")))]
      ; I don't know why, but server sends "ok" instead of :ok
      (if (some #{error} '("ok" :ok))
        (-> input .-classList (.remove "error"))
        (do
          (-> (get-element (name id)) .-classList (.add "error"))
          (set! (-> error-text .-innerText) (last error))))))

(defn update-details-status [status]
  (doseq [pair (seq status)]
    (update-error (first pair) (last pair))))

(defn handle-submit-error [body]
  (if (map? body)
    (update-details-status body)
    (println (str "Error processing result: " body))))

(defn submit-details []
  (let [details (gather-details)]
    (prn details)
    (go (let [result (<! (submit-request details))]
          (match (:status result)
                 200 (do
                       (set-details-visibility false)
                       (update-view))
                 400 (handle-submit-error (:body result)))))))

(d/defcomponent Controls [props]
  [:div.row.controls
   [:input {:type "text" :id "search-bar" :placeholder "Find something..."}]
   [:button {:on-click (fn [e] (search))} "Search"]
   [:button {:on-click (fn [e] (add-patient))} "Add a patient"]])

(defn delete-patient [id]
  (go (let [response (<! (http/delete (str "patient/" id)))]
        (update-view))))

(defn fill-details [patient]
  (doseq [prop-name ["fullname" "gender" "birthdate" "address" "insurancenum"]]
    (set-input prop-name ((keyword "patients" prop-name) patient))))

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
    [:label "Full name:"
     [:input {:type "text"
              :id "fullname"
              :placeholder "John Doe"
              :on-blur (fn [e] (update-error :fullname (v/validate-fullname (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Gender:" [:select {:id "gender"}
                       [:option {:value "female"} "Female"]
                       [:option {:value "male"} "Male"]
                       [:option {:value "other"} "Other"]]
     [:span.error-text "Error text."]]
    [:label "Birthdate:"
     [:input {:type "date"
              :id "birthdate"
              :placeholder "1981-12-31"
              :on-blur (fn [e] (update-error :birthdate (v/validate-birthdate (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Address:"
     [:input {:type "text"
              :id "address"
              :placeholder "Finland"
              :on-blur (fn [e] (update-error :address (v/validate-address (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Insurance #:"
     [:input {:type "number"
              :id "insurancenum"
              :placeholder "01101111010101101"
              :on-blur (fn [e] (update-error :insurancenum (v/validate-insurance-num (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
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

; TODO show nicer errors instead of dumping what server have sent
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
