# stringer release changelog

## TODO

- [TODO] If args to `strfmt` are literals then directly `.append` them without `nil` check
- [TODO] Fix `strfmt` to support the default width qualifier for `%f`


## 0.3.0 / 2016-October-13

- Fix #1 `strfmt` to accept format-string as non-literal, eval'ed as a string at compile time
- Efficiently repeat a string: `repstr`
- Efficiently repeat a character: `repchar`
- Efficient generation of string-representation of a table: `strtbl`


## 0.2.0 / 2016-April-21

- New macro `strfmt` - faster string formatting than `clojure.core/format`
  - Supports `%%`, `%b`, `%d`, `%f`, `%h`, `%n`, `%o`, `%s` and `%x` conversion specifiers
  - Supports upper-case variants `%B`, `%H`, `%S` and `%X` conversion specifiers
  - Does NOT support flags and width qualifiers in format specifiers


## 0.1.2 / 2015-December-16

- fix issue in `strcat` where a token was being evaluated twice


## 0.1.1 / 2015-June-01

- Optimize `join!` by using loop-recur instead of `interpose`


## 0.1.0 / 2015-May-20

- New macro `strcat` for faster string concatenation than `clojure.core/str`
- New macro `strdel` for faster string joining than `clojure.string/join`
