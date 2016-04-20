# Changelog

## 0.2.0 / - 2016-April-??

- New macro `strfmt` - faster `clojure.core/format`
  - Supports only `%d`, `%f` and `%s` without width qualifiers


## 0.1.2 / 2015-December-16

- fix issue in `strcat` where a token was being evaluated twice


## 0.1.1 / 2015-June-01

- Optimize `join!` by using loop-recur instead of `interpose`


## 0.1.0 / 2015-May-20

- New macro `strcat` for faster string concatenation than `clojure.core/str`
- New macro `strdel` for faster string joining than `clojure.string/join`

