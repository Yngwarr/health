(ns dev
  (:require [figwheel.main]
            [figwheel.main.api]
            [ring.adapter.jetty :refer [run-jetty]]
            [health.backend :refer [app]]
            [health.database :as db]
            [next.jdbc.types :refer [as-date]]))

(defn cljs []
  (if (get @figwheel.main/build-registry "dev")
    (figwheel.main.api/cljs-repl "dev")
    (figwheel.main.api/start "dev")))

(defn random-name []
  (str (rand-nth ["Fabio" "Luca" "Sascha" "Alex" "Daniele"]) " "
       (rand-nth ["Lione" "Turilli" "Paeth" "Staropoli" "Carbonera"])))

(defn random-date []
  (str (+ 1910 (rand-int 100)) "-" (+ 1 (rand-int 11)) "-" (+ 1 (rand-int 27))))

(defn populate-db [amount]
  (doseq [x (range amount)]
    (db/add-patient {:fullname (random-name)
                     :gender (rand-nth ["male" "female" "other"])
                     :birthdate (random-date)
                     :address (rand-nth ["Finland" "Real World"])
                     :insurancenum (str (+ 100000 (rand-int 999999999)))})))

(comment
  (def server (run-jetty app {:port 8080 :join? false}))
  (.stop server)
  (populate-db 10)
  (cljs)
  )
