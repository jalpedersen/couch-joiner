(ns joiner.main
  (:gen-class)
  (:use [com.ashafa.clutch]
        [joiner.core]
        [joiner.user]
        [clojure.contrib.json]
        [clojure.contrib.http.agent :only (http-agent string)]
        [clojure.contrib.command-line]))


(defn- update-doc [id doc file]
  (let [existing (get-document (:_id (merge {:_id id} doc)))
	updated-doc (if (empty? doc)
		      ;;If document is empty - don't bother updating it
		      existing
		      (update-document (merge existing doc)))]
    (if (nil? file)
      updated-doc
      (update-attachment updated-doc file))))

(defn -main [& args]
  (with-command-line args
    "Joiner - bending CouchDB"
    [[db "Database" nil]
     [op "Operation: get, save, update or delete" nil] 
     [id "Document id"]
     [doc "Document" "{}"]
     [file "File to attach"]
     remaining]
    (let [missing-options (filter #(nil? (:op %))
				  [{:op db :name "db"} {:op op :name "op"}])]
      (if (empty? missing-options)
	(with-db (authenticated-database db)
	  (let [json-doc (read-json doc)]
	    (println
	     (case (keyword op)
		   :get (get-document id)
		   :save (create-document json-doc)
		   :update (update-doc id json-doc file)
		   :delete (delete-document json-doc)
		   (str "Unknown operation " op)))))
	(println (str "Missing options: " (seq (map #(:name %) missing-options))))))))
	      
	    
  
