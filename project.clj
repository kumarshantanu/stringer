(defproject stringer "0.2.0"
  :description "Fast string operations for Clojure"
  :url "https://github.com/kumarshantanu/stringer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[citius "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.10"]]}
             :c16 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :c17 {:dependencies [[org.clojure/clojure "1.7.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :c18 {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :dlnk {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :perf {:dependencies [[citius "0.2.2"]]
                    :test-paths ["perf"]
                    :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]}}
  :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :plugins [[lein-cascade "0.1.2"]]
  :cascade {"test" [["clean"]
                    ["with-profile" "c16,dev" "test"]
                    ["clean"]
                    ["with-profile" "c17,dev" "test"]
                    ["clean"]
                    ["with-profile" "c18,dev" "test"]]
            "perf" [["clean"]
                    ["with-profile" "c16,dev,perf" "test"]
                    ["clean"]
                    ["with-profile" "c17,dev,perf" "test"]
                    ["clean"]
                    ["with-profile" "c18,dev,perf" "test"]]})
