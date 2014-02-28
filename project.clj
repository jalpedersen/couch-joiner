(defproject org.signaut/couch-joiner "1.3.0"
            :description "Manage couchdb users and databases"
            :url "http://github.com/jalpedersen/couch-joiner"
            :dependencies [[org.clojure/clojure "1.5.0"]
                           [org.clojure/tools.logging "0.2.3"]
                           [org.clojure/tools.cli "0.3.1"]
                           [cheshire "5.3.1"]
                           [org.clojure/data.codec "0.1.0"]
                           [com.ashafa/clutch "0.5.0-RC1"]]
            :main joiner.main
            :uberjar-name "couch-joiner.jar"
            :warn-on-reflection true)
