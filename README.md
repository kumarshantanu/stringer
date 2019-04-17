# stringer

A Clojure library for fast string operations. (See the [Development](#development) section to run performance
benchmarks.)


## Installation

Leiningen coordinates: `[stringer "0.4.0-SNAPSHOT"]`


## Usage

### Requiring namespace

```clojure
(require '[stringer.core :as s])
```

### String concatenation

`strcat` is a macro that accepts arguments to concatenate as a string.

```clojure
(s/strcat "foo" :bar 707 nil 'baz)
=> "foo:bar707baz"
```

### Joining string tokens by delimiter

`strdel` is a macro that accepts delimiter as first argument, and tokens as
the remaining arguments.

```clojure
(s/strdel ", " "foo" :bar 707 nil 'baz)
=> "foo, :bar, 707, , baz"
```

### Formatting string

#### Positional parameters

`strfmt` is a macro that accepts format-string as first argument, and format
parameters as arguments.

```clojure
(s/strfmt "Hello %s, here are %d products costing %f each." "human" 42 23.35)
=> "Hello human, here are 42 products costing 23.350000 each."
```

Note: Only limited format-specifier support exists. See [CHANGES.md](CHANGES.md) for details.

#### Named parameters

`nformat` is a macro that accepts format-string as first argument, and format
parameters as local vars or map argument.

```clojure
(let [guest "human"
      nprod 42
      pcost 23.35]
  (s/nformat "Hello {guest}, here are {nprod} products costing {pcost} each."))
=> "Hello human, here are 42 products costing 23.35 each."
;; OR
(s/nformat "Hello {guest}, here are {nprod} products costing {pcost} each."
           {:guest "human"
            :nprod 42
            :pcost 23.35})
=> "Hello human, here are 42 products costing 23.35 each."
```

The `fmt` macro, on the other hand, prepares a function to render a
format-string at a later point.

```clojure
(def x (s/fmt "Hello {guest}, here are {nprod} products costing {pcost} each."))
(x {:guest "human"
    :nprod 42
    :pcost 23.35})
=> "Hello human, here are 42 products costing 23.35 each."
```

### Work with [java.lang.StringBuilder](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html)

```clojure
(s/with-obj-str w
  (.append w "foo")
  (.append w 707))
=> "foo707"
```

There are `append!`, `join-all!` and `join!` macros to work with `StringBuilder`.

```clojure
(s/with-obj-str w
  (s/append! w "foo" :bar 707 nil 'baz)
  (s/join-all! w ", " :quux :norf)
  (s/join! w \| ["quick" "brown" "fox"]))
=> "foo:bar707baz:quux, :norfquick|brown|fox"
```

The local can be bound to something else too:

```clojure
(s/with-obj-str [w (java.util.ArrayList.)]
  (.add w 1)
  (.add w 2)
  (.add w 3))
=> "[1, 2, 3]"
```

### Printing tables

The `strtbl` function generates string representation of a textual table equivalent to `clojure.pprint/print-table`.

```clojure
(println (s/strtbl
           [{:name "Bill" :age 32 :gender :male}
            {:name "Anna" :age 47 :gender :female}
            {:name "Ravi" :age 39 :gender :male}]))
```


### Caveats

* Stringer uses macros to inline the code. Of course, they cannot be used like functions.
* If you see exception like this
   `Exception in thread "main" java.lang.IllegalArgumentException: More than one matching method found: append, compiling:(...)`
   Maybe you have `nil` as one of the non type-hinted arguments? Provide a type hint `^Object`.
* The `strfmt` macro expects the first argument, i.e. the format string, to be a string at compile time.


## Development

### Running performance benchmarks

With Clojure 1.8: `lein with-profile c08,perf do clean, test`

With Clojure 1.9: `lein with-profile c09,perf do clean, test`

With Clojure 1.10: `lein with-profile c10,perf do clean, test`

To run with Clojure 1.8, 1.9 and 1.10 in order: `lein cascade perf`

_If you are running the tests on a laptop, connect it to the power supply (so that the CPU is not clocked down) and
turn the screensaver/suspend off._


## License

Copyright © 2015-2019 Shantanu Kumar

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
