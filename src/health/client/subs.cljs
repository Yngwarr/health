(ns health.client.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [health.client.db :refer [find-patient]]))

; -------- [LAYER 1] --------

(reg-sub
  :patients
  (fn [db _] (:patients db)))

(reg-sub
  :loading?
  (fn [db _] (:loading? db)))

(reg-sub
  :now-editing
  (fn [db _] (:now-editing db)))

(reg-sub
  :now-showing
  (fn [db _] (:now-showing db)))

; -------- [LAYER 2] --------

(reg-sub
  :shown-patient
  (fn []
    [(subscribe [:patients])
     (subscribe [:now-showing])])
  (fn [[patients shown-id] _]
    (prn (find-patient shown-id patients))
    (find-patient shown-id patients)))
