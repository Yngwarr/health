(ns health.client.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [health.common.validation :as v]))

; -------- [HELPERS] --------

(defn get-element [id]
  (-> js/document (.getElementById id)))

(defn set-input
  ([id] (set-input id ""))
  ([id default] (set! (.-value (get-element id)) default)))

(defn clear-details []
  (doseq [id ["fullname" "birthdate" "address" "insurancenum"]]
    (set-input id))
  (set-input "gender" "female"))

(defn fill-details [patient]
  (doseq [prop-name ["fullname" "gender" "birthdate" "address" "insurancenum"]]
    (set-input prop-name (get patient (str "patients/" prop-name)))))

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

(defn clear-details-errors []
  (doseq [input (-> js/document (.querySelectorAll "#details input, #details select"))]
    (-> input .-classList (.remove "error"))))

(defn gather-details []
  {:fullname (.-value (get-element "fullname"))
   :gender (.-value (get-element "gender"))
   :birthdate (.-value (get-element "birthdate"))
   :address (.-value (get-element "address"))
   :insurancenum (.-value (get-element "insurancenum"))})

; -------- [VIEW] --------

(defn search-bar []
  [:div.row.controls
   [:input#search-bar {:type "text"
                       :placeholder "Find something..."
                       :on-change #(dispatch [:search-hit (.-value (get-element "search-bar"))])}]
   [:button
    {:on-click #(dispatch [:search-hit (.-value (get-element "search-bar"))])}
    "Search"]
   [:button
    {:on-click #(dispatch [:add-patient])}
    "Add a patient"]])

(defn actions [id]
  [:div
   [:button.action.material-symbols-outlined
    {:on-click #(dispatch [:edit-patient id])} "edit"]
   [:button.action.material-symbols-outlined
    {:on-click #(dispatch [:delete-patient id])} "delete"]])

(defn patients-table []
  [:table#patients
   [:thead
    [:tr [:th "ID"]
     [:th "Full Name"]
     [:th "Gender"]
     [:th "Birthdate"]
     [:th "Address"]
     [:th "Insurance #"]
     [:th "Actions"]]]
   [:tbody
    (let [patients @(subscribe [:patients])]
      (for [p patients]
        (let [id (get p "patients/id")]
          [:tr {:key id}
           [:td id]
           [:td [:a {:href (str "/#/p/" id)} (get p "patients/fullname")]]
           [:td (get p "patients/gender")]
           [:td (get p "patients/birthdate")]
           [:td (get p "patients/address")]
           [:td (get p "patients/insurancenum")]
           [:td [actions id]]])))]])

(defn edit-modal []
  [:div {:class (str "modal-background"
                     (when (nil? @(subscribe [:now-editing])) " hidden"))}
   [:div#details.modal
    [:label "Full name:"
     [:input#fullname
      {:type "text"
       :placeholder "John Doe"
       :on-blur (fn [e] (update-error :fullname (v/validate-fullname (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Gender:" [:select#gender
                       [:option {:value "female"} "Female"]
                       [:option {:value "male"} "Male"]
                       [:option {:value "other"} "Other"]]
     [:span.error-text "Error text."]]
    [:label "Birthdate:"
     [:input#birthdate
      {:type "date"
       :placeholder "1981-12-31"
       :on-blur (fn [e] (update-error :birthdate (v/validate-birthdate (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Address:"
     [:input#address
      {:type "text"
       :placeholder "Finland"
       :on-blur (fn [e] (update-error :address (v/validate-address (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:label "Insurance #:"
     [:input#insurancenum
      {:type "number"
       :placeholder "01101111010101101"
       :on-blur (fn [e] (update-error :insurancenum (v/validate-insurance-num (-> e .-target .-value))))}]
     [:span.error-text "Error text."]]
    [:div.row
     [:button
      ;{:on-click (fn [e] (submit-details))}
      {:on-click #(dispatch [:submit-details (gather-details)])}
      "Submit"]
     [:button
      {:on-click #(dispatch [:hide-details])}
      "Cancel"]]]])

(defn patient-modal []
  (when (some? @(subscribe [:now-showing]))
    [:div {:class (str "modal-background")}
     (let [patient @(subscribe [:shown-patient])]
       [:div#patient-win.modal
        [:h1 (str (get patient "patients/fullname") " ")
         [:span.material-symbols-outlined
          (let [gender (get patient "patients/gender")]
            (if (= gender "other")
              "transgender"
              gender))]]
        [:p [:b "Birth date: "]
         ;(let [birthdate (get patient "patients/birthdate")
               ;birth-year (js/parseInt (subs birthdate 0 4) 10)
               ;current-year (.getFullYear (new js/Date))]
           ;(str birthdate " (" (- current-year birth-year) " y. o.)"))
           (get patient "patients/birthdate")
         ]
        [:p [:b "Address: "] (get patient "patients/address")]
        [:p [:b "Insurance #: "] (get patient "patients/insurancenum")]
        [:button {:on-click #(dispatch [:hide-patient])} "Close"]])]))

(defn loading-modal []
  [:div {:class (str "modal-background"
                     (when (not @(subscribe [:loading?])) " hidden"))}
   [:div#lodaing.modal
    [:p "Loading..."]]])

(defn client-app []
  [:<>
   [search-bar]
   [patients-table]
   [edit-modal]
   [patient-modal]
   [loading-modal]])
