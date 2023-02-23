(ns health.client.db)

(def default-db
  {:search-query ""
   :now-editing nil ; nil, :new or int
   :loading? false
   :patients []})
