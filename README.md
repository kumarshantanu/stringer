# stringer

A Clojure library for fast string operations.

## Usage

### Requiring namespace

```clojure
(require '[stringer.core :as s])
```

### String concatenation

```clojure
(s/strcat "foo" :bar 707 nil 'baz)
=> "foo:bar707baz"
```

### Joining string tokens by delimiter

```clojure
(s/strjoin ", " "foo" :bar 707 nil 'baz)
=> "foo, :bar, 707, , baz"
```

### Work with [java.lang.StringBuilder](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html)

```clojure
(s/with-obj-str w
  (.append "foo")
  (.append 707))
=> foo707
```

## License

Copyright © 2015 Shantanu Kumar

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
