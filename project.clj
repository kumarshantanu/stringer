(defproject stringer "0.1.2"
  :description "Fast string operations for Clojure"
  :url "https://github.com/kumarshantanu/stringer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[citius "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.10"]]}
             :c16 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :c17 {:dependencies [[org.clojure/clojure "1.7.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :c18 {:dependencies [[org.clojure/clojure "1.8.0-RC3"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}}
  :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :test-selectors {:default (complement :perf)
                   :perf :perf
                   :strcat :strcat
                   :strjoin :strjoin}
  :plugins [[lein-cascade "0.1.2"]]
  :cascade {"test" [["clean"]
                    ["with-profile" "c16,dev" "test"]
                    ["clean"]
                    ["with-profile" "c17,dev" "test"]]
            "perf" [["clean"]
                    ["with-profile" "c16,dev" "test" ":perf"]
                    ["clean"]
                    ["with-profile" "c17,dev" "test" ":perf"]]})
