(ns health.database
  (:require [clojure.core.match :refer [match]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [insert!, query, update!, delete!]]
            [next.jdbc.types :refer [as-date]]))

(set! *print-namespace-maps* false)

(def db {:dbtype "postgres"
         :dbname "postgres"
         :user "postgres"
         :password "deathstar"
         :host "localhost"
         :port 5432})

(def ds (jdbc/get-datasource db))

(defn dates->str [patients]
  (map #(assoc % :patients/birthdate (str (:patients/birthdate %))) patients))

(defn all-patients []
  (query ds ["select * from patients"]))

(defn add-patient [info]
  (try
    (let [entry (assoc info :birthdate (as-date (:birthdate info)))
          result (insert! ds :patients entry)]
      (match result
        [:fail error] result
        :else :ok))
    (catch Throwable e [:fail (ex-message e)])))

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

(defn patch-patient [id info]
  (try
    (let [entry (assoc info :birthdate (as-date (:birthdate info)))
          result (update! ds :patients entry {:id id})]
      (if (> (:next.jdbc/update-count result) 0)
        :ok
        :not-found))
    ; TODO catch PSQLException instead
    ; TODO handle different cases somehow
    (catch Throwable e [:fail (ex-message e)])))

(comment
  (update! ds :patients {:address "Miami"} {:id 27})
  (patch-patient 27 {:birthdate "cucumber"})
  (query ds ["select * from patients"])
  (str (:patients/birthdate (first (query ds ["select birthdate from patients where id = 16"]))))
  (dates->str (all-patients))
  (query ds ["select * from patients where (fullname || ' ' || gender || ' ' || birthdate || ' ' || address || ' ' || insurancenum) like ?" "%Alex%"])
  (find-patients "Alex")
  (delete-patient "6")
  (:next.jdbc/update-count (delete-patient 10))
  (add-patient {:fullname "Matt Judge"
                :gender "male"
                :birthdate (as-date "1994-08-02")
                :address "AU"
                :insurancenum "9999111199991111"})
  (as-date "1999-09-09")
  )
