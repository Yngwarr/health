(ns health.routing)

(defn app-raw [request]
  (prn request)
  {:status 200 :body "You!"})

(defn routes [& configs]
  )
