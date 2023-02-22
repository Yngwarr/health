(ns health.client.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
  :patients
  (fn [db _] (:patients db)))
