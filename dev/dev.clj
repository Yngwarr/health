(ns dev
  (:require [figwheel.main]
            [figwheel.main.api]
            [ring.adapter.jetty :refer [run-jetty]]
            [health.backend :refer [app]]))

(defn cljs []
  (if (get @figwheel.main/build-registry "dev")
    (figwheel.main.api/cljs-repl "dev")
    (figwheel.main.api/start "dev")))

(comment
  (def server (run-jetty app {:port 8080 :join? false}))
  (.stop server)
  (cljs)
  )
