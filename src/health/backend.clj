(ns health.backend
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [health.database :refer [all-users]]))

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
   :body (all-users)})

(defroutes app-raw
  (GET "/user/:id" [id :as request] (page-user request))
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/patients" [request] (page-patients request))
  (route/resources "/")
  page-404)

(def app (-> app-raw wrap-json-response))

(comment
  (app {:request-method :get :uri "/index.html"})
  (app {:request-method :post :uri "/users"})

  (def server (run-jetty app {:port 8080 :join? false}))
  (.stop server)
  )
