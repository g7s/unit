(defproject unit "0.1.1"
  :description "Clojure(Script) library for dealing with units and conversions"
  :url "https://github.com/g7s/unit"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [org.clojure/spec.alpha "0.2.176" :scope "provided"]
                 [net.cgrand/macrovich "0.2.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :profiles {:test {:dependencies [[g7s/algo.generic "0.1.0"]]}})
