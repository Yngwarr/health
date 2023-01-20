(ns health.database
  (:require [next.jdbc :as jdbc]))

(def db {:dbtype "postgres"
         :dbname "postgres"
         :user "postgres"
         :password "deathstar"
         :host "localhost"
         :port 8080})

(def ds (jdbc/get-datasource db))
