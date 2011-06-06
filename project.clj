(defproject org.signaut/joiner "0.8"
  :description "Manage couchdb users and databases"
  :url "http://github.com/jalpedersen/joiner"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [com.ashafa/clutch "0.2.4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-eclipse "1.0.0"]
                     [lein-clojars "0.6.0"]]
  :main joiner.main
  :warn-on-reflection true
  )
