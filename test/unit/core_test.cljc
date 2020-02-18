(ns unit.core-test
  (:refer-clojure :exclude [second])
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.algo.generic.math-functions :refer [approx=]]
   [unit.core :as u :refer [to-si convert div to-unit prefix]]))


(def ^:dynamic *epsilon* 0.00001)


(defn eq
  [a b]
  (approx= a b *epsilon*))


(deftest test-to-si
  (testing "on predefined units"
    ;; Length
    (is (eq (to-si 1 u/foot) 0.3048))
    (is (eq (to-si 1 u/inch) 0.0254))
    (is (eq (to-si 1 u/yard) 0.9144))
    (is (eq (to-si 1 u/mile) 1609.344))
    ;; Mass
    (is (eq (to-si 1 u/gram) 0.001))
    (is (eq (to-si 1 u/pound) 0.45359237))
    (is (eq (to-si 1 u/ounce) 0.0283495231))
    ;; Time
    (is (eq (to-si 1 u/minute) 60))
    (is (eq (to-si 1 u/hour) 3600))
    (is (eq (to-si 1 u/day) (* 24 3600)))
    ;; Temperature
    (is (eq (to-si 0 u/celsius) 273.15))
    (is (eq (to-si 45 u/fahrenheit) 280.37222222222226))
    ;; Energy
    (is (eq (to-si 1 u/calorie) 4.18400))
    ;; Speed
    (is (eq (to-si 100 u/km-per-hour) 27.77777777777778))
    (is (eq (to-si 1 u/miles-per-hour) 0.44704))
    ;; Force
    (is (eq (to-si 4.3 u/newton) 4.3))
    (is (eq (to-si 1 u/dyn) 0.00001))
    (is (eq (to-si 1 u/kilopond) 9.80665))
    (is (eq (to-si 1 u/poundal) 0.138255))
    (is (eq (to-si 1 u/poundforce) 4.448222))
    ;; Power
    (is (eq (to-si 1 u/watt) 1))
    ;; Frequency
    (is (eq (to-si 1 u/hertz) 1)))
  (testing "on composed units"
    (is (eq (to-si 1 (div u/joule u/hour)) 1/3600))
    (is (eq (to-si 1/1000 (div u/kilopond u/inch u/inch)) 15.2003379006758))))


(deftest test-convert
  (is (eq (convert 12 u/meter u/inch) 472.4409448818898))
  (is (eq (convert 300 u/calorie u/joule) 1255.2))
  (is (eq (convert 12 u/meter u/inch) 472.4409448818898))
  (is (eq (convert 0 u/celsius u/fahrenheit) 31.999999999999936)) ; Should we round?
  (is (thrown-with-msg? Exception #"different dimensions" (convert 0 u/meter u/newton))))


(deftest test-prefix
  (is (eq (to-si 0.2 (prefix u/meter :k)) 200.0))
  (is (eq (to-si 200 (prefix u/meter "m")) 0.2))
  (is (eq (to-si 10 (prefix u/celsius :k)) 10273.15))
  (is (thrown-with-msg? Exception #"Unsupported prefix" (prefix u/second :r)))
  (is (thrown-with-msg? Exception #"does not support prefix" (prefix u/inch :k))))


(deftest test-to-unit
  (is (some? (to-unit "kg")))
  (is (some? (to-unit "mm")))
  (is (some? (to-unit "C")))
  (is (some? (to-unit "kph")))
  (is (thrown-with-msg? Exception #"Unrecognised unit" (to-unit "foo")))
  (is (thrown-with-msg? Exception #"Unrecognised unit" (to-unit "")))
  (is (thrown-with-msg? Exception #"Unrecognised unit" (to-unit nil))))
