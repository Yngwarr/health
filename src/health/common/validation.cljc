(ns health.common.validation)

(defn validate-birthdate [date]
  (cond
    (not (string? date)) [:fail "Birthdate must be a string."]
    ; TODO guard against invalid combinations of day and month
    (not (re-matches #"\d{4}-\d{2}-\d{2}" date)) [:fail "Birthdate format should be YYYY-MM-DD"]
    :else :ok))

(defn is-odd? [n]
  (= (mod n 2) 1))

(defn digit-root
  "repeated sum of all digits, base 10"
  [n]
  (if (zero? n)
    0
    (let [r (mod n 9)]
      (if (zero? r) 9 r))))

(defn mod10 [payload-str]
  (let [sum (atom 0)
        to-double (atom (is-odd? (count payload-str)))]
    (doseq [digit-char payload-str]
      (let [digit (Character/digit digit-char 10)]
        (swap! sum #(+ % (if @to-double (digit-root (* 2 digit)) digit)))
        (swap! to-double not)))
    (mod (- 10 (mod @sum 10)) 10)))

(defn mod10-check [value]
  (let [payload (subs value 0 (dec (count value)))
        control-digit (Character/digit (last value) 10)]
    (= (mod10 payload) control-digit)))

(defn validate-insurance-num [value]
  (cond
    (not (string? value))
      [:fail "Insuarnce number must be a string."]
    (not (re-matches #"\d{16}" value))
      [:fail "Insurance number must consist of 16 digits."]
    (not (mod10-check value))
      [:fail "Incorrect insurance number."]
    :else :ok))

(defn validate-patient [info]
  (if (map? info)
    {:fullname (if (empty? (:fullname info))
                 [:fail "Full name must be set."]
                 :ok)
     :gender (if (some #{(:gender info)} '("female" "male" "other"))
               :ok
               [:fail "Expected 'male', 'female' or 'other' as gender."])
     :birthdate (validate-birthdate (:birthdate info))
     :address (if (empty? (:address info))
                [:fail "Address must be set."]
                :ok)
     :insurancenum (validate-insurance-num (:insurancenum info))}
    [:fail "Expected a map."]))

(comment
  (mod10-check "1230")
  (subs "1230" 0 (dec (count "1230")))
  (validate-patient {:fullname "Matt Judge"
    :gender "male"
    :birthdate "1994-08-02"
    :address "AU"
    :insurancenum "0000000000001230"}))
