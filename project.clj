(defproject stringer "0.1.0-SNAPSHOT"
  :description "Fast string operations for Clojure"
  :url "https://github.com/kumarshantanu/stringer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}
             :c16 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :c17 {:dependencies [[org.clojure/clojure "1.7.0-beta1"]]}}
  :jvm-opts ^:replace ["-server"]
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :test-selectors {:default (complement :perf)
                   :perf :perf
                   :strcat :strcat
                   :strjoin :strjoin}
  :plugins [[lein-cascade "0.1.2"]]
  :cascade {"test" [["with-profile" "c16,dev" "test"]
                    ["with-profile" "c17,dev" "test"]]
            "perf" [["with-profile" "c16,dev" "test" ":perf"]
                    ["with-profile" "c17,dev" "test" ":perf"]]})
