# stringer

A Clojure library for fast string operations. (See the [Development](#development) section to run performance
benchmarks.)


## Installation

Leiningen coordinates: `[stringer "0.1.2"]`


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

### Caveats

* Stringer uses macros to inline the code. Of course, they cannot be used like functions.
* If you see exception like this
   `Exception in thread "main" java.lang.IllegalArgumentException: More than one matching method found: append, compiling:(...)`
   Maybe you have `nil` as one of the non type-hinted arguments? Provide a type hint `^Object`.


## Development

### Running performance benchmarks

With Clojure 1.6: `lein with-profile c16,dev do clean, test :perf`

With Clojure 1.7: `lein with-profile c17,dev do clean, test :perf`

To run with both Clojure 1.6 and 1.7 in order: `lein cascade perf`

_If you are running the tests on a laptop, connect it to the power supply (so that the CPU is not clocked down) and
turn the screensaver/suspend off._


## License

Copyright © 2015 Shantanu Kumar (kumar.shantanu@gmail.com, shantanu.kumar@concur.com)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
