(ns joiner.main
  (:gen-class)
  (:use com.ashafa.clutch)
  (:use joiner.core)
  (:use joiner.user)
  (:use clojure.contrib.json)
  (:use clojure.contrib.command-line))

(defn- save-doc [database doc]
  (let [db (get-secure-database database)
	existing
	(with-db db
	  (get-document (:_id doc)))]
    (with-db db
      (update-document (merge existing doc)))))

(defn- attach-file [database doc file]
  (with-db (get-secure-database database)
    (update-attachment doc file)))

(defn -main [& args]
  (with-command-line args
    "Joiner - bending CouchDB"
    [[db "Database"]
     [doc "Document to save"]
     [file "File to attach"]
     remaining]
    (if (nil? db)
      (println "Specify database")
      (if (nil? doc)
	(println "Specify document")
	(let [new-doc (save-doc db (read-json doc))]
	  (if (nil? file)
	    (println new-doc)
	    (println (attach-file db new-doc file))))))))
  