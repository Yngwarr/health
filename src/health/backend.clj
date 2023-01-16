(ns health.backend
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]))

(defn page-user [request]
  (let [user (-> request :params :id)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (format "Hello there, %s!" user)}))

(defn page-404 [request]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Page not found. :("})

(defroutes app
  (GET "/user/:id" [id :as request] (page-user request))
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/")
  page-404)

(comment
  (app {:request-method :get :uri "/index.html"})
  (app {:request-method :post :uri "/users"})

  (def server (run-jetty app {:port 8080 :join? false}))
  (.stop server)
  )
