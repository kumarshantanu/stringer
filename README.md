# stringer

A Clojure library for fast string operations.

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

`strjoin` is a macro that accepts delimiter as first argument, and tokens as
the remaining arguments.

```clojure
(s/strjoin ", " "foo" :bar 707 nil 'baz)
=> "foo, :bar, 707, , baz"
```

### Work with [java.lang.StringBuilder](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html)

```clojure
(s/with-obj-str w
  (.append w "foo")
  (.append w 707))
=> "foo707"
```

There are `append!` and `join!` macros to work with `StringBuilder`.

```clojure
(s/with-obj-str w
  (s/append! w "foo" :bar 707 nil 'baz)
  (s/join! w ", " :quux :norf))
=> "foo:bar707baz:quux, :norf"
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

## License

Copyright © 2015 Shantanu Kumar

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
