(ns health.client.views)

(defn search-bar []
  [:div.row.controls
   [:input#search-bar {:type "text" :placeholder "Find something..."}]
   ; TODO add on-click handlers
   [:button "Search"]
   [:button "Add a patient"]])

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
    ; TODO patient info
    ]
   ])

(defn modal []
  [:div.hidden#modal-background
   [:div.modal#details
    [:label "Full name:"
     [:input#fullname
      {:type "text"
       :placeholder "John Doe"
       ;:on-blur (fn [e] (update-error :fullname (v/validate-fullname (-> e .-target .-value))))
       }]
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
       ;:on-blur (fn [e] (update-error :birthdate (v/validate-birthdate (-> e .-target .-value))))
       }]
     [:span.error-text "Error text."]]
    [:label "Address:"
     [:input#address
      {:type "text"
       :placeholder "Finland"
       ;:on-blur (fn [e] (update-error :address (v/validate-address (-> e .-target .-value))))
       }]
     [:span.error-text "Error text."]]
    [:label "Insurance #:"
     [:input#insurancenum
      {:type "number"
       :placeholder "01101111010101101"
       ;:on-blur (fn [e] (update-error :insurancenum (v/validate-insurance-num (-> e .-target .-value))))
       }]
     [:span.error-text "Error text."]]
    [:div.row
     [:button
      ;{:on-click (fn [e] (submit-details))}
      "Submit"]
     [:button
      ;{:on-click (fn [e] (set-details-visibility false))}
      "Cancel"]]]])

(defn client-app []
  [:<>
   [search-bar]
   [patients-table]])
