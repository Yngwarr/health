(ns health.database
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [insert!, query, update!, delete!]]
            [next.jdbc.types :refer [as-date]]))

(def db {:dbtype "postgres"
         :dbname "postgres"
         :user "postgres"
         :password "deathstar"
         :host "localhost"
         :port 5432})

(def ds (jdbc/get-datasource db))

(defn all-patients []
  (jdbc/execute! ds ["select * from patients"]))

(defn add-patient [info]
  (insert! ds :patients info))

(comment
  (query ds ["select * from patients"])
  (delete! ds :patients {:fullname "Mattew Judge"})
  (insert! ds :patients {:fullname "Matt Judge"
                              :gender "male"
                              :birthdate (as-date "1994-08-02")
                              :address "AU"
                              :insurancenum 88888})
  (as-date "1999-09-09")
  )
