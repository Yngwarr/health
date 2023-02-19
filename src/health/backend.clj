(ns health.backend
  (:require [clojure.core.match :refer [match]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET POST DELETE PATCH defroutes]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [muuntaja.middleware :as mw]
            [next.jdbc :refer [get-datasource]]
            [health.database :as db]
            [health.routing :as routing]
            [health.common.validation :refer [validate-patient]]))

; TODO move database configuration to config files
(def backend-ds
  (get-datasource {:dbtype "postgres"
                   :dbname "postgres"
                   :user "postgres"
                   :password "deathstar"
                   :host "localhost"
                   :port 5432}))

(defn page-404 [request]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Page not found. :("})

(defn get-patients [ds request]
  {:status 200
   :body (let [query-text (-> request :params :q)]
           (if (nil? query-text)
             (db/dates->str (db/all-patients ds))
             (db/dates->str (db/find-patients ds query-text))))})

(defn delete-patient [ds id]
  (try
    (let [result (db/delete-patient ds (Integer/parseInt id))]
      (match result
        :ok {:status 200}
        :not-found {:status 404}
        [:fail error] {:status 500 :body error}))
    (catch NumberFormatException e
      {:status 400 :body "Expected a number."})))

(defn patch-patient [ds request]
  (try
    (let [id (Integer/parseInt (-> request :route-params :id))
          info (:body-params request)
          validation-result (validate-patient info)]
      (match validation-result
        :ok (let [result (db/patch-patient ds id info)]
              (match result
                :ok {:status 200}
                :not-found {:status 404}
                [:fail error] {:status 500 :body error}))
        [:fail error] {:status 400 :body error}
        _ {:status 400 :body validation-result}))
    (catch NumberFormatException e
      {:status 400 :body "Expected a number as an id."})))

(defn add-patient [ds request]
  (try
    (let [info (:body-params request)
          validation-result (validate-patient info)]
      (match validation-result
        :ok (let [result (db/add-patient ds (:body-params request))]
              (match result
                :ok {:status 200}
                [:fail error] {:status 500 :body error}))
        [:fail error] {:status 400 :body error}
        _ {:status 400 :body validation-result}))
    (catch Throwable e
      {:status 500 :body (ex-message e)})))

;(defroutes app-raw
  ;(GET "/" [] (resource-response "index.html" {:root "public"}))
  ;(GET "/patients" request (get-patients backend-ds request))
  ;(POST "/patient" request (add-patient backend-ds request))
  ;(DELETE "/patient/:id" [id] (delete-patient backend-ds id))
  ;(PATCH "/patient/:id" request (patch-patient backend-ds request))
  ;(route/resources "/")
  ;page-404)

(def app-raw routing/app-raw)

(def app (-> app-raw
             wrap-json-response
             mw/wrap-format
             wrap-keyword-params
             wrap-params
             ))

(defn -main [& args]
  (println "Starting the server...")
  (run-jetty app {:port 8080 :join? true}))

(comment
  (get-patients backend-ds {:params {:q "Rock"}})
  (add-patient backend-ds {:body-params {:fullname "Igor"
                                         :gender "male"
                                         :birthdate "1342-12-12"
                                         :address "Los Santos"
                                         :insurancenum "1234123412341238"}}))
