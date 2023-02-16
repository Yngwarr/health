(ns health.backend-test
  (:require [clojure.test :refer [deftest is run-test testing]]
            [clojure.java.shell :refer [sh]]
            [kaocha.repl :as k]
            [next.jdbc :as jdbc]
            [health.backend :as sut]))

(def testing-ds
  (jdbc/get-datasource {:dbtype "postgres"
                        :dbname "postgres"
                        :user "postgres"
                        :password "deathstar"
                        :host "localhost"
                        :port 5555}))

(def user-data {:fullname "Test User"
                :gender "male"
                :birthdate "1974-01-02"
                :address "Bournemouth, England, UK"
                :insurancenum "1234123412341238"})

(def search-user-data [user-data
                       (assoc user-data :fullname "Dwayne 'The Rock' Johnson")
                       {:fullname "Second User"
                        :gender "other"
                        :birthdate "2000-01-02"
                        :address "Blackrock, Ireland"
                        :insurancenum "1000000000000008"}])

(def user-data-result
  (into {} (map (fn [[key val]] [(keyword "patients" (name key)) val]) (seq user-data))))

(defn strip-ids [response]
  (assoc response :body (map #(dissoc % :patients/id) (:body response))))

(defn get-patient [tx query]
  (-> (sut/get-patients tx {:params {:q query}}) :body first :patients/id str))

(deftest valid-requests
  (jdbc/with-transaction [tx testing-ds]
    (testing "starting empty"
      (is (= {:status 200 :body '()} (sut/get-patients tx {}))))
    (testing "adding"
      (is (= {:status 200} (sut/add-patient tx {:body-params user-data})))
      (is (= {:status 200} (sut/add-patient tx {:body-params (assoc user-data :fullname "Another User")})))
      (is (= {:status 200
              :body '({:patients/fullname "Test User"
                       :patients/gender "male"
                       :patients/birthdate "1974-01-02"
                       :patients/address "Bournemouth, England, UK"
                       :patients/insurancenum "1234123412341238"}
                      {:patients/fullname "Another User"
                       :patients/gender "male"
                       :patients/birthdate "1974-01-02"
                       :patients/address "Bournemouth, England, UK"
                       :patients/insurancenum "1234123412341238"})}
             (strip-ids (sut/get-patients tx {})))))
    (testing "deleting"
      (let [id (get-patient tx "Another")]
        (is (= {:status 200} (sut/delete-patient tx id))))
      (is (= {:status 200
              :body '({:patients/fullname "Test User"
                       :patients/gender "male"
                       :patients/birthdate "1974-01-02"
                       :patients/address "Bournemouth, England, UK"
                       :patients/insurancenum "1234123412341238"})}
             (strip-ids (sut/get-patients tx {})))))
    (testing "patching"
      (let [id (get-patient tx "Test User")]
        (is (= {:status 200}
               (sut/patch-patient tx {:route-params {:id id}
                                      :body-params (assoc user-data :fullname "Goro Majima")}))))
      (is (= {:status 200
              :body '({:patients/fullname "Goro Majima"
                       :patients/gender "male"
                       :patients/birthdate "1974-01-02"
                       :patients/address "Bournemouth, England, UK"
                       :patients/insurancenum "1234123412341238"})}
             (strip-ids (sut/get-patients tx {})))))
    (.rollback tx)))

(deftest validation
  (jdbc/with-transaction [tx testing-ds]
    (testing "validation"
      (testing "nonsense body"
        (is (= {:status 400 :body "Expected a map."}
               (sut/add-patient tx {}))))
      (testing "empty address"
        (is (= {:status 400 :body {:address [:fail "Address must be set."]
                                  :birthdate :ok
                                  :fullname :ok
                                  :gender :ok
                                  :insurancenum :ok}}
              (sut/add-patient tx {:body-params (assoc user-data :address "")}))))
      (testing "birthdate validation"
        (testing "empty string"
          (is (= {:status 400 :body {:address :ok
                                     :birthdate [:fail "Birthdate format should be YYYY-MM-DD"]
                                     :fullname :ok
                                     :gender :ok
                                     :insurancenum :ok}}
                 (sut/add-patient tx {:body-params (assoc user-data :birthdate "")}))))
        (testing "wrong format"
          (is (= {:status 400 :body {:address :ok
                                     :birthdate [:fail "Birthdate format should be YYYY-MM-DD"]
                                     :fullname :ok
                                     :gender :ok
                                     :insurancenum :ok}}
                 (sut/add-patient tx {:body-params (assoc user-data :birthdate "08/12/2005")})))))
      (testing "empty fullname"
        (is (= {:status 400 :body {:address :ok
                                   :birthdate :ok
                                   :fullname [:fail "Full name must be set."]
                                   :gender :ok
                                   :insurancenum :ok}}
               (sut/add-patient tx {:body-params (assoc user-data :fullname "")}))))
      (testing "empty gender"
        (is (= {:status 400 :body {:address :ok
                                   :birthdate :ok
                                   :fullname :ok
                                   :gender [:fail "Expected 'male', 'female' or 'other' as gender."]
                                   :insurancenum :ok}}
               (sut/add-patient tx {:body-params (assoc user-data :gender "")}))))
      (testing "insurance number validation"
        (testing "empty"
          (is (= {:status 400 :body {:address :ok
                                     :birthdate :ok
                                     :fullname :ok
                                     :gender :ok
                                     :insurancenum [:fail "Insurance number must consist of 16 digits."]}}
                (sut/add-patient tx {:body-params (assoc user-data :insurancenum "")}))))
        (testing "wrong number"
          (is (= {:status 400 :body {:address :ok
                                     :birthdate :ok
                                     :fullname :ok
                                     :gender :ok
                                     :insurancenum [:fail "Incorrect insurance number."]}}
                 (sut/add-patient tx {:body-params (assoc user-data :insurancenum "1234123412341234")})))))
      (testing "table should be empty"
        (is (= {:status 200 :body '()} (sut/get-patients tx {})))))
    (.rollback tx)))

(deftest patching
  (jdbc/with-transaction [tx testing-ds]
    (sut/add-patient tx {:body-params user-data})
    (testing "patching"
      (testing "nonsense id"
        (is (= {:status 400 :body "Expected a number as an id."}
               (sut/patch-patient tx {:route-params {:id ""} :body-params user-data}))))
      (testing "non-existent id"
        (is (= {:status 404} (sut/patch-patient tx {:route-params {:id "99"} :body-params user-data}))))
      (testing "validation"
        (is (= {:status 400 :body {:address :ok,
                                   :birthdate :ok,
                                   :fullname [:fail "Full name must be set."],
                                   :gender :ok,
                                   :insurancenum :ok}}
               (sut/patch-patient tx {:route-params {:id "1"}
                                      :body-params (assoc user-data :fullname "")}))))
      (testing "existent insurance number"
        (sut/add-patient tx {:body-params user-data})
        (let [result (sut/add-patient tx {:body-params user-data})]
          (is (= {:status 400 :body {:address :ok,
                                     :birthdate :ok,
                                     :fullname :ok,
                                     :gender :ok,
                                     :insurancenum [:fail "Insurance number must be unique."]}}
                 (sut/patch-patient tx {:route-params {:id (-> result :body :patients/id str)}
                                        :body-params (assoc user-data :insurancenum (:insurancenum user-data))})))))
      (testing "table should contain correct data only"
        (is (= {:status 200 :body (list user-data-result)}
               (let [res (sut/get-patients tx {})] (strip-ids res))))))
    (.rollback tx)))

(deftest deleting
  (jdbc/with-transaction [tx testing-ds]
    (sut/add-patient tx {:body-params user-data})
    (testing "deleting"
      (testing "nonsense id"
        (is (= {:status 400 :body "Expected a number."} (sut/delete-patient tx ""))))
      (testing "non-existent id"
        (is (= {:status 404} (sut/delete-patient tx "99"))))
      (testing "correct id"
        (let [id (get-patient tx "")]
          (is (= {:status 200} (sut/delete-patient tx id))))))
    (testing "table should be empty after the deletion"
      (is (= {:status 200 :body '()} (sut/get-patients tx {}))))
    (.rollback tx)))

(deftest searching
  (jdbc/with-transaction [tx testing-ds]
    (doseq [p search-user-data]
      (sut/add-patient tx {:body-params p}))
    (testing "searching"
      (testing "names"
        (is (= {:status 200
                :body (list {:patients/address "Bournemouth, England, UK"
                             :patients/birthdate "1974-01-02"
                             :patients/fullname "Test User"
                             :patients/gender "male"
                             :patients/insurancenum "1234123412341238"}
                            {:patients/address "Blackrock, Ireland"
                             :patients/birthdate "2000-01-02"
                             :patients/fullname "Second User"
                             :patients/gender "other"
                             :patients/insurancenum "1000000000000008"})}
               (strip-ids (sut/get-patients tx {:params {:q "User"}})))))
      (testing "locations and names"
        (is (= {:status 200
                :body (list {:patients/address "Bournemouth, England, UK"
                             :patients/birthdate "1974-01-02"
                             :patients/fullname "Dwayne 'The Rock' Johnson"
                             :patients/gender "male"
                             :patients/insurancenum "1234123412341238"})}
               (strip-ids (sut/get-patients tx {:params {:q "Rock"}})))))
      (testing "genders"
        (is (= {:status 200
                :body (list {:patients/address "Blackrock, Ireland"
                             :patients/birthdate "2000-01-02"
                             :patients/fullname "Second User"
                             :patients/gender "other"
                             :patients/insurancenum "1000000000000008"})}
               (strip-ids (sut/get-patients tx {:params {:q "other"}})))))
      (testing "dates"
        (is (= {:status 200
                :body (list {:patients/address "Blackrock, Ireland"
                             :patients/birthdate "2000-01-02"
                             :patients/fullname "Second User"
                             :patients/gender "other"
                             :patients/insurancenum "1000000000000008"})}
               (strip-ids (sut/get-patients tx {:params {:q "2000"}})))))
      (testing "insurance numbers"
        (is (= {:status 200
                :body (list {:patients/address "Bournemouth, England, UK"
                             :patients/birthdate "1974-01-02"
                             :patients/fullname "Test User"
                             :patients/gender "male"
                             :patients/insurancenum "1234123412341238"}
                            {:patients/address "Bournemouth, England, UK"
                             :patients/birthdate "1974-01-02"
                             :patients/fullname "Dwayne 'The Rock' Johnson"
                             :patients/gender "male"
                             :patients/insurancenum "1234123412341238"})}
               (strip-ids (sut/get-patients tx {:params {:q "1234"}})))))
      )
    (.rollback tx)))

(comment
  (run-test patching)
  (k/run :unit))
