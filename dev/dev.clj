(ns dev
  (:require [clojure.string :refer [join]]
            [figwheel.main]
            [figwheel.main.api :as fig]
            [ring.adapter.jetty :refer [run-jetty]]
            [health.backend :refer [app backend-ds]]
            [health.database :as db]
            [next.jdbc.types :refer [as-date]]
            [health.common.validation :refer [mod10 mod10-check]]))

(defn cljs []
  (if (get @figwheel.main/build-registry "dev")
    (fig/cljs-repl "dev")
    (fig/start-join "dev")))

(defn random-name []
  (str (rand-nth ["Fabio" "Luca" "Sascha" "Alex" "Daniele"]) " "
       (rand-nth ["Lione" "Turilli" "Paeth" "Staropoli" "Carbonera"])))

(defn random-date []
  (str (+ 1910 (rand-int 100)) "-" (+ 1 (rand-int 11)) "-" (+ 1 (rand-int 27))))

(defn random-insurancenum []
  (let [base (join (repeatedly 15 #(str (rand-int 10))))]
    (str base (mod10 base))))

(defn populate-db [amount]
  (doseq [x (range amount)]
    (db/add-patient backend-ds
                    {:fullname (random-name)
                     :gender (rand-nth ["male" "female" "other"])
                     :birthdate (random-date)
                     :address (rand-nth ["Finland" "Real World"])
                     :insurancenum (random-insurancenum)})))

(comment
  (defn opp [& args] (prn args) {:status 404})
  (def server (run-jetty app {:port 8080 :join? false}))
  (.stop server)
  (populate-db 10)
  (rand-int 10))
