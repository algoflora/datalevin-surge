(defproject io.github.algoflora/datalevin-surge "0.1.0"
  :description "Datalevin database migrations plugin for Leiningen"
  :url "https://github.com/datalevin-surge"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :eval-in-leiningen true

  :jvm-opts ["--add-opens=java.base/java.nio=ALL-UNNAMED"
             "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"]

  :plugins [[lein-eftest "0.6.0"]]
  
  :dependencies [[datalevin "0.9.5"]
                 [comb "0.1.1"]])

