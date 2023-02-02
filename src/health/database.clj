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

(set! *print-namespace-maps* false)

(defn all-patients []
  (query ds ["select * from patients"]))

(defn add-patient [info]
  (insert! ds :patients info))

(defn find-patients [query-text]
  ; TODO make search more intellegent
  (query ds [(str "select * from patients where (fullname || ' ' || gender || ' '"
                  " || birthdate || ' ' || address || ' ' || insurancenum) like ?")
             (str "%" query-text "%")]))

(defn delete-patient [id]
  (try
    (let [result (delete! ds :patients {:id id})]
     (if (> (:next.jdbc/update-count result) 0)
       :ok
       :not-found))
    ; TODO catch PSQLException instead
    (catch Throwable e [:fail (ex-message e)])))

(comment
  (query ds ["select * from patients"])
  (query ds ["select * from patients where id = 5"])
  (query ds ["select * from patients where (fullname || ' ' || gender || ' ' || birthdate || ' ' || address || ' ' || insurancenum) like ?" "%Alex%"])
  (find-patients "Alex")
  (delete-patient "6")
  (:next.jdbc/update-count (delete-patient 10))
  (insert! ds :patients {:fullname "Matt Judge"
                         :gender "male"
                         :birthdate (as-date "1994-08-02")
                         :address "AU"
                         :insurancenum 88888})
  (as-date "1999-09-09")
  )
