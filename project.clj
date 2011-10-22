(defproject org.signaut/joiner "0.9.0"
  :description "Manage couchdb users and databases"
  :url "http://github.com/jalpedersen/joiner"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/tools.cli "0.1.0"]
                 [org.clojure/data.json "0.1.1"]
		 [com.ashafa/clutch "0.2.5"]]
  :dev-dependencies [[swank-clojure "1.3.3"]
                     [lein-eclipse "1.0.0"]
                     [lein-clojars "0.7.0"]]
  :main joiner.main
  :warn-on-reflection true
  )
