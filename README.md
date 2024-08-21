# datalevin-surge

[![Clojars Project](https://img.shields.io/clojars/v/io.github.algoflora/datalevin-surge.svg)](https://clojars.org/io.github.algoflora/datalevin-surge)

Utility for seamless migrations of Datalevin databases in Leiningen projects.

## Usage

_These instructions are for Leiningen projects._

_TODO: add ability to use this tool with **Clojure CLI** and **Babashka**_

### Setup

Add to your `project.clj` something like this:

```clojure
:aliases {"surge" ["run" "-m" "datalevin-surge.core"]}
:datalevin-surge {:migrations-dir "./resources/migrations"
                  :profiles {:dev  "./dtlv"
                             :prod "dtlv://datalevin:datalevin@127.0.0.1"}}
```

`:migrations-dir` is the relative path to the folder with migrations code
`:profiles` is a map with different datalevin connection strings

### CLI

* In the beginning (regarding to `surge` alias in example) you have to initialize the tool:

```lein surge :dev init```

It will create the initial migratoin in migrations folder with current schema of database specified in profile (`:dev` in example case).
* Another usefull command can help you to check integrity of data. It checks codebase and database if they are initialized to use **datalevin-surge** and if migrations data is consistent:

```lein surge :dev check```
* Now you can create the new migration:

```lein surge :dev new [<migration_name>]```

It will create new migration file names `<migration_name>` (it will ask for name if there is empty in command). Synthax of file is explained in next paragraph.
* To do migration you should use:

```lein surge :dev up <number_of_migrations>```

It will apply `number_of_migrations`. This option must be a positive integer number of migrations to apply in a row. If it is greater then available count, then all available migrations will be applied. Another option to apply all available migrations is to use `:all` keyword.
* To revert migrations you can do almost the same:

```lein surge :dev down <number_of_migrations>```

* And one more command just inform you about status (applied or not) of all migrations you have:

```lein surge :dev status```

_TODO: Add convient `help` command_

### Syntax

This is the example with comments of migration file created by `new` command:

```clojure
;;; Datalevin Surge migration
;;;
;;;   Migration name                         ; name of migration passed when created
;;;
;;;   Project: example-project               ; project name from `project.clj`
;;;
;;;   I'am doing nothing FIXME               ; description you can freely change
;;;
;;;   Initialy created: 2024.08.21 22:06:55  ; date and time when migration was created

{
 ;; UUID of migration. If you edit it you will break whole the system. 
 :uuid #uuid "381e1c9b-25dd-4e7d-bfc9-4b803ba9df8e" ; DO NOT EDIT!

 ;; UUID of parent migration (can be `nil` for initial migration).
 ;; Edit it ONLY if you totally understand what you are doing
 ;; (like inserting manually migration in between existing ones).
 :parent #uuid "4ac08b96-87a2-416f-a49d-7debda119dac"                         

 :up {
      ;; Staging function.
      ;; Accepts Datalevin connection and should return the data to be saved between migrations.
      ;; Could be `nil`.
      :stage-fn (fn [conn]
                  (into {} (datalevin.core/q (quote [:find ?p ?amount
                                                     :where [?p :person/amount ?amount]])
                                             (datalevin.core/db conn))))

      ;; Vector of entities attributes to remove from schema.
      ;; All data under such attributes also will be deleted!
      ;; Use `:stage-fn` to save necessary information.
      ;; Cound be `nil`.
      :schema-remove [:person/amount]

      ;; Schema attributes to insert in existing schema.
      ;; Could be `nil`.
      :schema-insert {:person/account {:db/valueType :db.type/ref
                                       :db/cardinality :db.cardinality/one}

                      :account/uuid {:db/valueType :db.type/uuid
                                     :db/cardinality :db.cardinality/one
                                     :db/unique :db.unique/identity}

                      :account/amount {:db/valueType :db.type/bigdec
                                       :db/cardinality :db.cardinality/one}}

      ;; Unstaging function.
      ;; Accept Datalevin connection and data, that was saved by `:stage-fn`.
      ;; This function applies after schema was changed to write saved data regarding to new data structure.
      ;; Could be `nil`.
      :unstage-fn (fn [conn stage]
                    (datalevin.core/transact! conn (mapcat (fn [[pid am]]
                                                             (let [aid (+ 1000 pid)]
                                                               [{:db/id aid
                                                                 :account/uuid (random-uuid)
                                                                 :account/amount am}
                                                                {:db/id pid
                                                                 :person/account aid}])) stage)))}

 ;; Same as in `:up` section but for reverting migration.
 :down {:stage-fn (fn [conn]
                    (into {} (datalevin.core/q (quote [:find ?p ?amount
                                                       :where
                                                       [?p :person/account ?acc]
                                                       [?acc :account/amount ?amount]]) (datalevin.core/db conn))))
        :schema-remove [:person/account :account/uuid :account/amount]
        :schema-insert {:person/amount {:db/valueType :db.type/bigdec
                                        :db/cardinality :db.cardinality/one}}
        :unstage-fn (fn [conn stage]
                      (datalevin.core/transact! conn (mapv (fn [[pid am]]
                                                             {:db/id pid
                                                              :person/amount am}) stage)))}}
```

### Functions limitations

`:stage-fn` and `:unstage-fn` have some limitations:
* you have to use fully qualified Datalevin functions like `datalevin.core/q` or `datalevin.core/db`
* you have to use `quote` instead of `'` like `(quote [:find ?p ?amount :where [?p :person/amount ?amount]])`
* for now you can't use side namespaces except `datalevin.core`

_TODO: add the ability to use side namespaces_

## Development

Contributors are welcome! You can check current todos on [Issues page](https://github.com/algoflora/datalevin-surge/issues) or add your own.

## License

Copyright © 2024 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
