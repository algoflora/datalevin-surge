(defproject io.github.algoflora/datalevin-surge "0.1.2"
  :description "Datalevin database migrations plugin for Leiningen"
  :url "https://github.com/datalevin-surge"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :jvm-opts ["--add-opens=java.base/java.nio=ALL-UNNAMED"
             "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
             "-XX:-OmitStackTraceInFastThrow"]

  :plugins [[lein-eftest "0.6.0"]]

  :main ^:skip-aot datalevin-surge.core
  
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [datalevin "0.9.10"]
                 [comb "0.1.1"]
                 [tick "0.7.5"]
                 [leiningen-core "2.11.2"]]

  :profiles {:test {:resource-paths ["test/resources"]}
             :repl {:resource-paths ["test/resources"]}}

  :eftest {:multithread? false})
