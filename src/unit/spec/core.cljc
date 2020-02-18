(ns unit.spec.core
  (:refer-clojure :exclude [second])
  (:require
   [clojure.spec.alpha :as s]
   [unit.core :as u]))



(s/def ::l int?)
(s/def ::m int?)
(s/def ::t int?)
(s/def ::I int?)
(s/def ::T int?)
(s/def ::N int?)
(s/def ::Iv int?)
(s/def ::known-dimensions u/property-dimension-map)
(s/def ::const-dimensions #(= % u/const-dimensions))
(s/def ::literal-dimensions (s/tuple ::l ::m ::t ::I ::T ::N ::Iv))
(s/def ::dimensions (s/or :known ::known-dimensions
                          :const ::const-dimensions
                          :literal ::literal-dimensions))


(s/def ::prefix-name u/prefixes)
(s/def ::prefix-literal pos?)
(s/def ::prefix (s/or :name ::prefix-name
                      :literal ::prefix-literal))


(s/def ::slope (s/and number? (complement zero?)))
(s/def ::y-intercept number?)
(s/def ::unit (s/tuple ::literal-dimensions ::slope ::y-intercept boolean?))


(defn- to-unit
  "Same as to-unit but return false if cannot parse unit."
  [s]
  (try
    (u/to-unit s)
    (catch Throwable e
      false)))


(defmacro def-property-spec
  [& args]
  `(do
     ~@(map (fn [spec-kw]
              (let [prop   (keyword (name spec-kw))
                    dim-eq `(fn [~'e]
                              (= (u/dimensions ~'e)
                                 (~prop u/property-dimension-map)))]
                `(s/def ~spec-kw (s/or :unit (s/and ::unit ~dim-eq)
                                       :string (fn [~'e]
                                                 (~dim-eq (to-unit ~'e)))))))
            args)))


(def-property-spec
  ::electric-current
  ::mass
  ::angular-momentum
  ::force
  ::speed
  ::time
  ::frequency
  ::energy
  ::power
  ::length
  ::linear-momentum
  ::luminus-intensity
  ::amount-of-substance
  ::acceleration
  ::temperature)


(s/fdef u/make-unit
  :args (s/alt :arity-2 (s/cat :dimensions any? :slope ::slope)
               :arity-3 (s/cat :dimensions any? :slope ::slope :y-intercept ::y-intercept)
               :arity-4 (s/cat :dimensions any?
                               :slope ::slope
                               :y-intercept ::y-intercept
                               :prefix? boolean?))
  :ret ::unit)


(s/fdef u/dimensions
  :args (s/cat :unit ::unit)
  :ret ::literal-dimensions)


(s/fdef u/slope
  :args (s/cat :unit ::unit)
  :ret ::slope)


(s/fdef u/y-intercept
  :args (s/cat :unit ::unit)
  :ret ::y-intercept)


(s/fdef u/conv-fn
  :args (s/cat :unit ::unit)
  :ret (s/fspec :args (s/cat :x number?)
                :ret number?)
  :fn #(= ((:ret %) 0) (u/y-intercept (get-in % [:args :unit]))))


(s/fdef u/inv-conv-fn
  :args (s/cat :unit ::unit)
  :ret (s/fspec :args (s/cat :x number?)
                :ret number?)
  :fn #(= ((:ret %) 0) (let [u (get-in % [:args :unit])] (/ (u/y-intercept u) (u/slope u)))))


(s/fdef u/div
  :args (s/cat :units (s/+ ::unit))
  :ret ::unit)


(s/fdef u/mult
  :args (s/cat :units (s/+ ::unit))
  :ret ::unit)


(s/fdef u/exp
  :args (s/cat :unit ::unit
               :exponent (s/and number? (complement zero?)))
  :ret ::unit)


(s/fdef u/prefix-factor
  :args (s/or :string string? :keyword keyword?)
  :ret ::prefix-literal)


(s/fdef u/prefix
  :args (s/cat :unit ::unit
               :prefix ::prefix-name)
  :ret ::unit)


(s/fdef u/dim-eq
  :args (s/alt :arity-1 ::unit
               :arity-2 (s/tuple ::unit ::unit))
  :ret boolean?)


(s/fdef u/dimensionless?
  :args (s/cat :unit ::unit)
  :ret boolean?)


(s/def ::conversion-fn
  (s/fspec :args (s/cat :magnitude number?
                        :unit ::unit)
           :ret number?))


(s/def to-si ::conversion-fn)


(s/def from-si ::conversion-fn)


(s/fdef u/convert
  :args (s/cat :magnitude number?
               :from-unit ::unit
               :to-unit ::unit)
  :ret ::unit)


(s/def ::unit-split (s/tuple string? string?))


(s/fdef u/unit-splits
  :args (s/cat :s string?)
  :ret (s/* ::unit-split)
  :fn #(<= (count (:ret %)) (count (:s (:args %)))))


(s/fdef u/split-to-prefix-unit
  :args (s/cat :unit-split ::unit-split)
  :ret (s/tuple (s/nilable ::prefix-literal) ::unit))


(s/fdef u/to-unit
  :args (s/cat :s string?)
  :ret ::unit)
