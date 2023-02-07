(ns health.backend
  (:require [clojure.core.match :refer [match]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET POST DELETE defroutes]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [muuntaja.middleware :as mw]
            [health.database :as db]))

(defn page-user [request]
  (let [user (-> request :params :id)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (format "Hello there, %s!" user)}))

(defn page-404 [request]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Page not found. :("})

(defn get-patients [request]
  {:status 200
   :body (let [query-text (-> request :params :q)]
           (if (nil? query-text)
             (db/all-patients)
             (db/find-patients query-text)))})

(defn delete-patient [id]
  (try
    (let [result (db/delete-patient (Integer/parseInt id))]
      (match result
             :ok {:status 200}
             :not-found {:status 404}
             [:fail error] {:status 500 :body error}))
    (catch NumberFormatException e
      {:status 400 :body "Expected a number."})))

(defn add-patient [request]
  (prn (:body-params request))
  (try
    ; TODO validate info
    (let [result (db/add-patient (:body-params request))]
      (match result
             :ok {:status 200}
             [:fail error] {:status 500 :body error}))
    (catch Throwable e
      {:status 500 :body (ex-message e)}))
  {:status 200})

(defroutes app-raw
  (GET "/user/:id" [id] (page-user id))
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/patients" request (get-patients request))
  (DELETE "/patient/:id" [id] (delete-patient id))
  (POST "/patient" request (add-patient request))
  (route/resources "/")
  page-404)

(def app (-> app-raw
             wrap-json-response
             mw/wrap-format
             wrap-keyword-params
             wrap-params))

(defn -main [& args]
  (println "Starting the server...")
  (run-jetty app {:port 8080 :join? true}))
