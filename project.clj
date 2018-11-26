(defproject stringer "0.3.1"
  :description "Fast string operations for Clojure"
  :url "https://github.com/kumarshantanu/stringer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :c05 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :c06 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :c07 {:dependencies [[org.clojure/clojure "1.7.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :c08 {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :c09 {:dependencies [[org.clojure/clojure "1.9.0"]]
                   :global-vars {*unchecked-math* :warn-on-boxed}}
             :c10 {:dependencies [[org.clojure/clojure "1.10.0-beta8"]]}
             :dlnk {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :perf {:dependencies [[citius "0.2.4"]]
                    :test-paths ["perf"]
                    :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]}}
  :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :plugins [[lein-cascade "0.1.2"]]
  :cascade {"test" [["clean"]
                    ["with-profile" "c05" "test"]
                    ["clean"]
                    ["with-profile" "c06" "test"]
                    ["clean"]
                    ["with-profile" "c07" "test"]
                    ["clean"]
                    ["with-profile" "c08" "test"]
                    ["clean"]
                    ["with-profile" "c09" "test"]
                    ["clean"]
                    ["with-profile" "c10" "test"]]
            "perf" [["clean"]
                    ["with-profile" "c05,perf" "test"]
                    ["clean"]
                    ["with-profile" "c06,perf" "test"]
                    ["clean"]
                    ["with-profile" "c07,perf" "test"]
                    ["clean"]
                    ["with-profile" "c08,perf" "test"]
                    ["clean"]
                    ["with-profile" "c09,perf" "test"]
                    ["clean"]
                    ["with-profile" "c10,perf" "test"]]})

