(ns health.client.events
  (:require
    [ajax.core :as ajax]
    [day8.re-frame.http-fx]
    [re-frame.core :refer [reg-event-fx reg-event-db]]
    [health.client.db :refer [default-db]]
    [health.client.views :refer [clear-details fill-details]]))

(reg-event-fx
  :init-db
  (fn [_ _]
    {:db default-db
     :fx [[:dispatch [:update-patients]]]}))

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
    (prn event)
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

; TODO implement
(reg-event-fx
  :edit-patient
  (fn [{:keys [db event]} [_ id]]
    (prn event)
    (let [patient (first (filter #(= (get % "patients/id") id) (:patients db)))]
      (prn patient)
      (fill-details patient))
    {:db (assoc db :now-editing id)}))

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
  :search
  (fn [{:keys [db]} [_ query]]
    {:db (assoc db :search-query query)
     :fx [[:dispatch [:update-patients]]]}))
