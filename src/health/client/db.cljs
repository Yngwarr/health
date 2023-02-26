(ns health.client.db)

(defn find-patient [id patients]
  (first (filter #(= (get % "patients/id") id) patients)))

(def default-db
  {:search-query ""
   :now-editing nil ; nil, :new or int
   :now-showing nil ; nil or int
   :loading? false
   :patients []})
