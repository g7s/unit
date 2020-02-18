# unit

[![Clojars Project](https://img.shields.io/clojars/v/unit.svg)](https://clojars.org/unit)

A Clojure(Script) library for dealing with units and conversions.

For some background on units see [here](https://g7s.io/post/units/)


## Installation

To install, add the following to your project `:dependencies`:

    [unit "0.1.0"]


## Usage

With this library you can define, derive, convert and recognize units of measurement.

### Definition

Define a unit with `make-unit` that has the form

```clojure
(make-unit dimensions slope y-intercept prefix?)
```

where `dimensions` is either a 7-element vector or a keyword from the [predefined dimensions](https://github.com/g7s/unit/blob/master/src/unit/core.cljc#L15)
`slope` is a non-zero number representing the conversion factor to the reference unit,
`y-intercept` is a number (usually 0 except for units like `Celcius` etc) and `prefix?`
a boolean that is true only when the unit accepts a prefix (defaults to `false`).

An example:

```clojure
(ns my.ns
  (:require
   [unit.core :refer [make-unit]))


;; Taking the reference unit to be the kilogram

(def gram (make-unit :mass 1e-3 0 true)
(def pound (make-unit :mass 0.45359237))
```

If your unit comes with a symbol it is better to use `defunit` (see [Recognition](#recognition))

```clojure
(defunit pound :lb (make-unit :mass 0.45359237))
```

### Derivation

Deriving new units from other units is done with the functions

* **mult** - For multiplying units
* **div** - For dividing units
* **exp** - For exponentiating a unit
* **prefix** - For applying a prefix to a prefix accepting unit

An example:

```clojure
(def watt-per-sq-meter-kelvin (div watt (mult (exp meter 2) kelvin)))
(def kcal-per-sq-foot-celsius-hour
  (div (prefix calorie :k)
       (mult (exp foot 2)
             (mult hour celsius))))
```


### Conversion

Convert a number (magnitude) from a unit to another using `convert` which has the form

```clojure
(convert magnitude unit-a unit-b)
```

where `magnitude` is the magnitude of a measurement expressed in `unit-a` units and
`unit-b` is the target unit for the conversion.

An example:

```clojure
(convert 12 pound gram) ;; => 5443.10844
```

Note that `convert` will throw an exception if you try to convert between incompatible units
(i.e. between units with different dimensions).


### Recognition

For every unit that has been defined with `defunit` (see [Definition](#definition)) and therefore
has been associated with a symbol, you can use `to-unit` that takes a string and returns a
(possibly prefixed) unit.

An example:

```clojure
(to-unit "kJ") ;; The kilojoule unit
```

Note that this will throw if it cannot find the unit symbol or if the recognised unit does not
accept a prefix and you passed a prefix or if the prefix is not supported.

Examples with exceptions

```clojure
(to-unit "klb") ;; Unit does not support prefix
(to-unit "rg")  ;; Unrecognised prefix r
(to-unit "foo") ;; Unrecognised unit
```

Recognition ends here, this library does not try to interpret an expression involving units in any way.


## License

Copyright © 2020 Gerasimos

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
