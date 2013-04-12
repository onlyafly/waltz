(defproject onlyafly/waltz "0.1.0-alpha2"
  :description "A ClojureScript library to keep your state in step"
  :url "http://github.com/onlyafly/waltz"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [prismatic/cljs-test "0.0.5"]]
  :cljsbuild {:builds 
              {:test {:source-paths ["src" "test"]
                      :compiler {:output-to "target/unit-test.js"
                                 :optimizations :whitespace
                                 :pretty-print true}}}
              :test-commands {"unit" ["phantomjs" "target/unit-test.js"]}}
  :plugins [[lein-cljsbuild "0.3.0"]]
  :hooks [leiningen.cljsbuild])

