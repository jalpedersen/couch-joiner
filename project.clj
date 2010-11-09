(defproject joiner "1.0.0-SNAPSHOT"
  :description "Manage couchdb users and databases"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [com.ashafa/clutch "0.2.3-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :main joiner.main
  :warn-on-reflection true
  )
