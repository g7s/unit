(ns unit.core
  (:refer-clojure :exclude [second])
  (:require
   [clojure.set :refer [map-invert]])
  #?(:clj
     (:require
      [clojure.math.numeric-tower :as math]
      [net.cgrand.macrovich :as macros])
     :cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [unit.core :refer [defunit]])))


(def property-dimension-map
  {:length              [1 0 0 0 0 0 0]
   :mass                [0 1 0 0 0 0 0]
   :time                [0 0 1 0 0 0 0]
   :electric-current    [0 0 0 1 0 0 0]
   :temperature         [0 0 0 0 1 0 0]
   :amount-of-substance [0 0 0 0 0 1 0]
   :luminus-intensity   [0 0 0 0 0 0 1]
   :frequency           [0 0 -1 0 0 0 0]
   :speed               [1 0 -1 0 0 0 0]
   :linear-momentum     [1 1 -1 0 0 0 0]
   :angular-momentum    [2 1 -1 0 0 0 0]
   :acceleration        [1 0 -2 0 0 0 0]
   :force               [1 1 -2 0 0 0 0]
   :energy              [2 1 -2 0 0 0 0]
   :power               [2 1 -3 0 0 0 0]})


(def dimension-property-map
  (map-invert property-dimension-map))


(def const-dimensions [0 0 0 0 0 0 0])


(def prefixes
  {:Y  1e24
   :Z  1e21
   :E  1e18
   :P  1e15
   :T  1e12
   :G  1e9
   :M  1e6
   :k  1e3
   :h  1e2
   :da 1e1
   :d  1e-1
   :c  1e-2
   :m  1e-3
   :μ  1e-6
   :mu 1e-6
   :n  1e-9
   :p  1e-12
   :f  1e-15
   :a  1e-18
   :z  1e-21
   :y  1e-24})


(defn valid-literal-dimensions?
  [dims]
  (and (vector? dims)
       (= 7 (count dims))
       (every? int? dims)))


(def valid-slope? (every-pred number? (complement zero?)))


(def units (atom {}))


(defn- get-dimensions
  "Return dimensions vector."
  [dims]
  (or (property-dimension-map dims)
      (and (valid-literal-dimensions? dims) dims)
      (throw (ex-info "Unrecognised dimensions"
                      {:error-type :unrecognised-dimensions
                       :dimensions dims}))))


(defn make-unit
  ([dimensions slope]
   (make-unit dimensions slope 0))
  ([dimensions slope y-intercept]
   (make-unit dimensions slope y-intercept false))
  ([dimensions slope y-intercept prefix?]
   {:pre [(valid-slope? slope)
          (number? y-intercept)
          (boolean? prefix?)]}
   ^{:type ::unit}
   [(get-dimensions dimensions) slope y-intercept prefix?]))


(macros/deftime
  (defmacro defunit
    [unit kw body]
    (let [body (if (map? body)
                 `(make-unit ~(:dimensions body)
                             ~(:slope body)
                             ~(:y-intercept body 0)
                             ~(:prefix? body false))
                 body)]
     `(do
        (def ~unit ~body)
        (swap! units assoc ~kw ~unit)))))


(defn dimensions
  [unit]
  (get unit 0))


(defn slope
  [unit]
  (get unit 1))


(defn y-intercept
  [unit]
  (get unit 2))


(defn prefix?
  [unit]
  (get unit 3))


(defn measures
  [unit]
  (dimension-property-map (dimensions unit) :unknown))


(defn- conv-fn
  "The conversion function of a unit."
  [unit]
  (fn [x] (+ (* x (slope unit)) (y-intercept unit))))


(defn- inv-conv-fn
  "The inverse conversion function of a unit."
  [unit]
  (fn [x] (/ (- x (y-intercept unit)) (slope unit))))


(defn div
  "The relative change operation."
  ([u] u)
  ([u1 u2]
   (make-unit (mapv - (dimensions u1) (dimensions u2))
              (/ (slope u1) (slope u2))))
  ([u1 u2 & rest]
   (reduce div (div u1 u2) rest)))


(defn mult
  "The mutliplicative change operation."
  ([u] u)
  ([u1 u2]
   (make-unit (mapv + (dimensions u1) (dimensions u2))
              (* (slope u1) (slope u2))))
  ([u1 u2 & rest]
   (reduce mult (mult u1 u2) rest)))


(def mult-identity (make-unit const-dimensions 1))


(defn exp
  "The k exponentiation operation."
  [u k]
  (if (= 1 k)
    u
    (make-unit (mapv #(* % k) (dimensions u))
               (#?(:clj math/expt :cljs js/Math.pow) (slope u) k))))


(defn- prefix-factor
  "Given a prefix return its value from the `prefixes` map."
  [ps]
  (or (prefixes (keyword ps))
      (throw (ex-info (str "Unsupported prefix: " (name ps))
                      {:error-type :unsupported-prefix
                       :prefixes   prefixes}))))


(defn- make-prefix-unit
  [u p]
  (if (prefix? u)
    (make-unit (dimensions u) (* (slope u) p) (y-intercept u))
    (throw (ex-info "Unit does not support prefixing"
                    {:error-type :unit-prefix-support}))))


(defn prefix
  "The prefix operation."
  [u pk]
  (make-prefix-unit u (prefix-factor pk)))


(defn dim-eq
  "Check if the units are dimensionally equivalent."
  ([u] true)
  ([u1 u2] (= (dimensions u1) (dimensions u2))))


(defn dimensionless?
  "Test whether the unit is dimensionless."
  [u]
  (= (dimensions u) const-dimensions))


;;;; Registered Units

(macros/usetime
;;; Length
 (defunit meter :m (make-unit :length 1 0 true))
 (defunit foot :ft (make-unit :length 0.3048))
 (defunit inch :in (make-unit :length 0.0254))
 (defunit yard :yd (make-unit :length 0.9144))
 (defunit mile :mi (make-unit :length 1609.344))

;;; Mass
 (defunit gram :g (make-unit :mass 1e-3 0 true))
 (defunit pound :lb (make-unit :mass 0.45359237))
 (defunit ounce :oz (make-unit :mass 0.0283495231))

;;; Time
 (defunit second :s (make-unit :time 1 0 true))
 (defunit minute :min (make-unit :time 60))
 (defunit hour :h (make-unit :time 3600))
 (defunit day :day (make-unit :time 86400))

;;; Temperature
 (defunit kelvin :K (make-unit :temperature 1 0 true))
 (defunit celsius :C (make-unit :temperature 1 273.15 true))
 (defunit fahrenheit :F (make-unit :temperature 0.55555555555 (* 0.55555555555 459.67)))

;;; Energy
 (defunit joule :J (make-unit :energy 1 0 true))
 (defunit calorie :cal (make-unit :energy 4.18400 0 true))

;;; Force
 (defunit newton :N (make-unit :force 1 0 true))
 (defunit dyn :dyn (make-unit :force 1e-5))
 (defunit kilopond :kp (make-unit :force 9.80665))
 (defunit poundal :pdl (make-unit :force 0.138255))
 (defunit poundforce :lbf (make-unit :force 4.448222))

;;; Power
 (defunit watt :W (make-unit :power 1 0 true))

;;; Frequency
 (defunit hertz :Hz (make-unit :frequency 1 0 true))

;;; Speed
 (defunit km-per-hour :kph (div (prefix meter :k) hour))
 (defunit miles-per-hour :mph (div mile hour))
 )


;;;; Unregistered units

(def B (make-unit const-dimensions 1 0 true))
(def KB (prefix B :k))
(def MB (prefix B :M))
(def GB (prefix B :G))
(def TB (prefix B :T))


;;;; Conversion

(defn to-si
  "Convert a magnitude from a unit `u` to the SI equivalent."
  [magnitude u]
  ((conv-fn u) magnitude))


(defn from-si
  "Convert a magnitude from SI to a unit `u`."
  [magnitude u]
  ((inv-conv-fn u) magnitude))


(defn convert
  "Convert a magnitude from unit `u1` to `u2`. Throw exception if not dimensionally equivalent."
  [magnitude u1 u2]
  (if (dim-eq u1 u2)
    (-> magnitude
        (to-si u1)
        (from-si u2))
    (throw (ex-info "Units have different dimensions."
                    {:error-type :convert-different-dimensions}))))


;;;; Recognition

(defn- split-tuples
  "Return a lazy sequence of tuples of substrings from the start of a string."
  ([^String s] (split-tuples s 0))
  ([^String s n]
   (when (some? str)
     (let [max (count s)]
       (if (> n max)
         (split-tuples s (mod n (+ 1 max)))
         (lazy-seq (cons [(subs s 0 n) (subs s n)] (split-tuples s (+ n 1)))))))))


(defn- unit-splits
  [s]
  (take (count s) (split-tuples s)))


(defn- split-to-prefix-unit
  [[ps us]]
  (when-let [u (get @units (keyword us))]
    [(when-not (empty? ps) (prefix-factor ps)) u]))


(defn- to-unit*
  "From the string `s` to the first valid prefixed unit accumulating errors."
  [s errors]
  (->> s
       unit-splits
       (keep #(try
                (when-let [[p u] (split-to-prefix-unit %)]
                  (if (nil? p)
                    u
                    (make-prefix-unit u p)))
                (catch #?(:clj Exception :cljs :default) e
                  (swap! errors conj e)
                  nil)))
       first))


(defn to-unit
  "Given a (optionally prefixed) unit symbol return a unit object or nil if no match.
  Throw exception on unsupported prefix or if a unit does not accept a prefix."
  [s]
  (let [errors (atom [])]
    (if-let [unit (to-unit* s errors)]
      unit
      (let [[e] @errors]
        (if e
          (throw e)
          (throw (ex-info "Unrecognised unit"
                          {:error-type :unrecognised-unit})))))))


(comment
  )
