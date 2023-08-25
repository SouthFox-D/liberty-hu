;; shadow-cljs configuration
(defproject liberty-hu "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [thheller/shadow-cljs "2.25.2"]
                 [com.google.javascript/closure-compiler-unshaded "v20230802"]
                 [ring/ring-core "1.11.0-alpha1"]
                 [ring/ring-jetty-adapter "1.11.0-alpha1"]
                 [org.babashka/http-client "0.4.14"]
                 [org.jsoup/jsoup "1.16.1"]
                 [cheshire "5.11.0"]

                 [org.slf4j/slf4j-nop "2.0.7"]
                 [reagent/reagent "1.2.0"]
                 [metosin/reitit "0.7.0-alpha5"]
                 [superstructor/re-frame-fetch-fx "0.2.0"]
                 [binaryage/devtools "1.0.7"]
                 [re-frame/re-frame "1.3.0"]
                 [day8.re-frame/tracing  "0.6.2"]
                 [day8.re-frame/re-frame-10x "1.6.0"]]

  :source-paths ["src/main"]
  :main ^:skip-aot backend.core

  :profiles
  {:uberjar {:dependencies ^:replace [[org.clojure/clojure "1.11.1"]
                                      [ring/ring-jetty-adapter "1.11.0-alpha1"]
                                      [metosin/reitit "0.7.0-alpha5"]
                                      [org.slf4j/slf4j-nop "2.0.7"]
                                      [org.babashka/http-client "0.4.14"]
                                      [org.jsoup/jsoup "1.16.1"]
                                      [cheshire "5.11.0"]]

             :aot :all}})
