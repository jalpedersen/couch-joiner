(defproject org.signaut/couch-joiner "1.2.1"
            :description "Manage couchdb users and databases"
            :url "http://github.com/jalpedersen/couch-joiner"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [org.clojure/tools.logging "0.2.3"]
                           [org.clojure/tools.cli "0.2.1"]
                           [cheshire "4.0.0"]
                           [org.clojure/data.codec "0.1.0"]
                           [com.ashafa/clutch "0.4.0-RC1"]]
            :plugins [[lein-clojars "0.9.1"]]
            :main joiner.main
            :uberjar-name "couch-joiner.jar"
            :warn-on-reflection true)
