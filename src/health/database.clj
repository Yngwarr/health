(ns health.database
  (:require [next.jdbc :as jdbc]))

(def db {:dbtype "postgres"
         :dbname "postgres"
         :user "postgres"
         :password "deathstar"
         :host "localhost"
         :port 5432})

(def ds (jdbc/get-datasource db))

(defn all-users []
  (jdbc/execute! ds ["select * from patients"]))

(comment
  (jdbc/execute! ds ["select * from patients"])
  )
