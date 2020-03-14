(defproject sky-deck "0.0.1"
  :description ""
  :url ""
  :license {:name ""
            :url  ""}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.11.0-rc3"]
                 [re-graph "0.1.11"]
                 [io.lettuce/lettuce-core "5.2.1.RELEASE"]
                 [medley "1.2.0"]
                 [aero "1.1.3"]
                 [metosin/porsas "0.0.1-alpha13"]
                 [honeysql "0.9.8"]
                 [nilenso/honeysql-postgres "0.2.6"]
                 [org.postgresql/postgresql "42.2.8"]
                 [seancorfield/next.jdbc "1.0.10"]
                 [ragtime "0.8.0"]
                 [io.lettuce/lettuce-core "5.2.2.RELEASE"]
                 [com.wsscode/pathom "2.2.28"]
                 [cambium/cambium.core "0.9.3"]
                 [cambium/cambium.codec-simple "0.9.3"]
                 [cambium/cambium.logback.core "0.4.3"]
                 [integrant "0.7.0"]
                 [yada "1.2.16"]
                 [bidi "2.1.6"]
                 [metosin/jsonista "0.2.3"]
                 [aleph "0.4.7-alpha2"]
                 [com.cerner/clara-rules "0.19.1"]
                 [funcool/cuerdas "2.2.0"]
                 [com.walmartlabs/lacinia "0.33.0"]
                 [buddy/buddy-hashers "1.4.0"]]
  :source-paths ["src" "lib"]
  :uberjar-name "sky-deck.jar"
  :main sky-deck.main
  :aot [sky-deck.main]
  :repl-options {:init-ns user}
  :plugins [[lein-zprint "0.3.12"]
            [lein-ancient "0.6.15"]
            [lein-eftest "0.5.3"]
            [lein-cloverage "1.1.2"]]
  :zprint {:old?   false
           :width  80
           :vector {:respect-nl? true}
           :set    {:sort? true}
           :map    {:justify?      true
                    :force-nl?     true
                    :sort?         true
                    :sort-in-code? false
                    :comma?        false}
           :style  :community}
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build"
            ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min" ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test" ["run"
                        "-m"
                        "figwheel.main"
                        "-co"
                        "test.cljs.edn"
                        "-m"
                        "sky-deck.test-runner"]
            "rebl" ["trampoline" "run" "-m" "rebel-readline.main"]}
  :profiles {:dev {:source-paths ["dev" "test" "lib"]
                   :dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [eftest "0.5.9"]
                                  [orchestra "2019.02.06-1"]
                                  [expound "0.7.2"]
                                  [ring/ring-mock "0.4.0"]
                                  [com.bhauman/rebel-readline "0.1.4"]
                                  [org.clojure/test.check "0.10.0"]
                                  [district0x/graphql-query "1.0.6"]
                                  [org.xerial/sqlite-jdbc "3.28.0"]
                                  [integrant/repl "0.3.1"]
                                  [mvxcvi/puget "1.2.0"]
                                  [reifyhealth/specmonstah "2.0.0"]
                                  [juxt/iota "0.2.3"]]}})
