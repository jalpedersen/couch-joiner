(defproject org.signaut/couch-joiner "1.1.2"
            :description "Manage couchdb users and databases"
            :url "http://github.com/jalpedersen/couch-joiner"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [org.clojure/tools.logging "0.2.3"]
                           [org.clojure/tools.cli "0.2.1"]
                           [org.clojure/data.json "0.1.2"]
                           [com.ashafa/clutch "0.3.1"]]
            :dev-dependencies [[lein-clojars "0.9.0"]]
            :main joiner.main
            :uberjar-name "couch-joiner.jar"
            :warn-on-reflection true)
