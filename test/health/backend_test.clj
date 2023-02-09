(ns health.backend-test
  (:require [clojure.test :refer [deftest is use-fixtures run-test]]
            [clojure.java.shell :refer [sh]]
            [kaocha.repl :as k]
            [health.backend :as sut]
            [health.database :refer [*testing* truncate-test]]))

(defn fix-db [t]
  (truncate-test)
  (binding [*testing* true] (t)))

(use-fixtures :once fix-db)

(deftest inserting
  ; TODO add inserting
  (is (= {:status 200 :body '()} (sut/get-patients {}))))

(comment
  (run-test getting)
  (k/run :unit)
  (prn *testing*))
