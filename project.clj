(defproject joiner "1.0.0-SNAPSHOT"
  :description "Manage couchdb users and databases"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [com.ashafa/clutch "0.2.4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-eclipse "1.0.0"]]
  :main joiner.main
  :warn-on-reflection true
  )
