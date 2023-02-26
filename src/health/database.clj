(ns health.database
  (:require [clojure.core.match :refer [match]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [insert! query update! delete!]]
            [next.jdbc.types :refer [as-date]]))

(set! *print-namespace-maps* false)

(defn dates->str [patients]
  (map #(assoc % :patients/birthdate (str (:patients/birthdate %))) patients))

(defn all-patients [ds]
  (query ds ["select * from patients"]))

(defn get-patient [ds id]
  (first (query ds ["select * from patients where id = ?" id])))

(defn add-patient [ds info]
  (try
    (let [entry (assoc info :birthdate (as-date (:birthdate info)))
          result (insert! ds :patients entry)]
      (match result
        [:fail error] result
        :else :ok))
    (catch Throwable e [:fail (ex-message e)])))

(defn find-patients [ds query-text]
  ; TODO make search more intellegent
  (query ds [(str "select * from patients where (fullname || ' ' || gender || ' '"
                  " || birthdate || ' ' || address || ' ' || insurancenum) like ?")
             (str "%" query-text "%")]))

(defn delete-patient [ds id]
  (try
    (let [result (delete! ds :patients {:id id})]
     (if (> (:next.jdbc/update-count result) 0)
       :ok
       :not-found))
    ; TODO catch PSQLException instead
    (catch Throwable e [:fail (ex-message e)])))

(defn patch-patient [ds id info]
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
  (as-date "1999-09-09"))
