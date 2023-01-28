(ns health.backend
  (:require [ring.util.response :refer [resource-response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [health.database :refer [all-patients]]))

(defn page-user [request]
  (let [user (-> request :params :id)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (format "Hello there, %s!" user)}))

(defn page-404 [request]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Page not found. :("})

(defn page-patients [request]
  {:status 200
   :body (all-patients)})

(defroutes app-raw
  (GET "/user/:id" [id :as request] (page-user request))
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/patients" [request] (page-patients request))
  (route/resources "/")
  page-404)

(def app (-> app-raw wrap-json-response))

(defn -main [& args]
  (println "Starting the server...")
  (run-jetty app {:port 8080 :join? true}))
