(ns health.client.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

; -------- [LAYER 1] --------

(reg-sub
  :patients
  (fn [db arg2] (:patients db)))

(reg-sub
  :loading?
  (fn [db _] (:loading? db)))

(reg-sub
  :now-editing
  (fn [db _] (:now-editing db)))
