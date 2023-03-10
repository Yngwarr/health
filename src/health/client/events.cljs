(ns health.client.events
  (:require
    [ajax.core :as ajax]
    [day8.re-frame.http-fx]
    [re-frame.core :refer [reg-event-fx reg-event-db reg-fx]]
    [health.client.db :refer [default-db find-patient]]
    [health.client.views :refer [clear-details fill-details update-details-status]]))

(defn change-location [location]
  (set! js/window.location location))

(reg-fx
  :change-location
  (fn [location]
    (change-location location)))

(reg-fx
  :set-query
  (fn [query]
    (change-location (if (empty? query) "/#" (str "/#/s/" query)))))

(reg-event-fx
  :init-db
  (fn [_ _]
    {:db default-db}))

(reg-event-fx
  :update-patients
  (fn [{:keys [db]}]
    (let [query (:search-query db)]
      {:db (assoc db :loading? true)
       :fx [[:http-xhrio {:method :get
                          :uri "patients"
                          :format (ajax/transit-request-format)
                          :params (if query {:q query} {})
                          :response-format (ajax/transit-response-format)
                          :on-success [:patients-updated]
                          :on-failure [:request-failed]}]]})))

; TODO show error window
(reg-event-fx
  :request-failed
  (fn [{:keys [db event]}]
    {:db (assoc db :loading? false)}))

(reg-event-fx
  :patients-updated
  (fn [{:keys [db]} [_ patients]]
    {:db (assoc db
                :patients patients
                :loading? false)}))

(reg-event-fx
  :add-patient
  (fn [{:keys [db]}]
    {:db (assoc db :now-editing :new)}))

(reg-event-fx
  :hide-details
  (fn [{:keys [db]}]
    (clear-details)
    {:db (assoc db :now-editing nil)}))

(reg-event-fx
  :edit-patient
  (fn [{:keys [db]} [_ id]]
    (fill-details (find-patient id (:patients db)))
    {:db (assoc db :now-editing id)}))

(reg-event-fx
  :show-patient
  (fn [{:keys [db event]} [_ id]]
    (prn event)
    (if (nil? (find-patient id (:patients db)))
      {:fx [[:dispatch [:get-patient id]]]}
      {:db (assoc db :now-showing id)})))

(reg-event-fx
  :get-patient
  (fn [{:keys [db event]} [_ id]]
    (prn event)
    {:http-xhrio {:method :get
                  :uri (str "patient/" id)
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [:add-single-patient]
                  :on-failure [:request-failed]}}))

(reg-event-fx
  :add-single-patient
  (fn [{:keys [db event]} [_ response]]
    {:db (assoc db :patients (vector response))
     :fx [[:dispatch [:show-patient (get response "patients/id")]]]}))

(reg-event-fx
  :hide-patient
  (fn [{:keys [db]} _]
    {:db (assoc db :now-showing nil)
     :fx [[:set-query (:search-query db)]]}))

(reg-event-fx
  :delete-patient
  (fn [_ [_ id]]
    {:http-xhrio {:method :delete
                  :uri (str "patient/" id)
                  :format (ajax/transit-request-format)
                  :response-format (ajax/text-response-format)
                  :on-success [:update-patients]
                  :on-failure [:request-failed]}}))

(reg-event-fx
  :search-hit
  (fn [_ [_ query]]
    {:fx [[:set-query query]]}))

(reg-event-fx
  :search
  (fn [{:keys [db]} [_ query]]
    {:db (assoc db :search-query query)
     :fx [[:dispatch [:update-patients]]]}))

(reg-event-fx
  :submit-details
  (fn [{:keys [db]} [_ details]]
    (let [now-editing (:now-editing db)]
      {:http-xhrio {:method (if (= now-editing :new) :post :patch)
                    :uri (if (= now-editing :new) "patient" (str "patient/" now-editing))
                    :params details
                    :format (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success [:details-submit-done]
                    :on-failure [:details-submit-fail]}})))

(reg-event-fx
  :details-submit-done
  (fn [_ [_ response]]
    (prn response)
    {:fx [[:dispatch [:hide-details]]
          [:change-location (str "/#/p/" (get response "patients/id"))]]}))

; TODO rewrite in the reactive way
(reg-event-fx
  :details-submit-fail
  (fn [{:keys [db]} [_ response]]
    (update-details-status (:response response))
    {:db db}))
