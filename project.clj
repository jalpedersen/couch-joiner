(defproject org.signaut/couch-joiner "1.1.0"
  :description "Manage couchdb users and databases"
  :url "http://github.com/jalpedersen/couch-joiner"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/tools.cli "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [com.ashafa/clutch "0.3.1"]]
  :dev-dependencies [[swank-clojure "1.3.3"]
                     [lein-clojars "0.7.0"]]
  :main joiner.main
  :warn-on-reflection true
  )
