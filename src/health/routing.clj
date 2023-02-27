(ns health.routing
  (:require [ring.util.response :refer [resource-response]]
            [clojure.string :refer [join]]))

; returns either map of route-params or nil
(defn match-path [target value]
  (cond
    (string? target) (if (= target value) {} nil)
    (vector? target)
    (let [pattern (re-pattern (join (map #(if (keyword? %) "(\\w+)" %) target)))
          match (re-matches pattern value)]
      (if match
        (zipmap (filter keyword? target) (subvec match 1))
        nil))))

(defn routes [rs]
  (fn [request]
    (let [handler (reduce
                   (fn [res [method uri h]]
                     (let [params (match-path uri (:uri request))]
                       (when (and (= method (:request-method request)) params)
                         (reduced #(h (assoc % :route-params params))))))
                   nil rs)]
      (if (some? handler)
        (handler request)
        (or (and (= :get (:request-method request))
                 (resource-response (:uri request) {:root "public"}))
            {:status 404})))))

(comment
  (def rs (routes [[:get "/" (fn [_] (resource-response "index.html" {:root "public"}))]
                   [:get "/patients" identity]
                   [:post "/patient" identity]
                   [:get ["/patient/" :id] identity]
                   [:delete ["/patient/" :id] identity]
                   [:patch ["/patient/" :id] identity]]))
  (rs {:request-method :post :uri "/"})
  (rs {:request-method :get :uri "/index.html"})
  (rs {:request-method :get :uri "/patient/19"})
  (let [target ["/patient/" :id "/" :what]
        ;value "/patient/19/what"
        value "/patient/"
        pattern (re-pattern (join (map #(if (keyword? %) "(\\w+)" %) target)))
        match (re-matches pattern value)
        ;params (zipmap (filter keyword? target) (subvec match 1))
        ]
    match)
  (match-path []))
