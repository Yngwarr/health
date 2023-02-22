(ns health.client.db
  (:require [cljs.spec.alpha :as s]))

;(s/def ::search-query string?)

;(s/def ::now-editing #(or (nil? %) (int? %)))

;(s/def ::patients/id int?)
;(s/def ::patients/fullname string?)
;(s/def ::patients/gender #{"female" "male" "other"})
;; TODO check format
;(s/def ::patients/birthdate string?)
;(s/def ::patients/address string?)
;; TODO validate number
;(s/def ::patients/insurancenum string?)

;(s/def ::patient
  ;(s/keys :req-un [::patients/id ::patients/fullname ::patients/gender
                   ;::patients/birthdate ::patients/address
                   ;::patients/insurancenum]))
;(s/def ::patients (s/every ::patient))

;(s/def ::db (s/keys :req-un [::search-query ::now-editing ::patients]))

(def default-db
  {:search-query ""
   :now-editing nil
   :loading? false
   :patients []})
