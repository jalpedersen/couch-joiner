(ns joiner.main
  (:gen-class)
  (:use [joiner.core]
        [joiner.user]
        [com.ashafa.clutch :only (update-document get-database
                                   put-document get-document
                                   put-attachment with-db
                                   delete-document)]
        [clojure.data.json]
        [clojure.tools.cli]))


(defn- update-doc [id doc file]
  (assert (not-empty id) "Document ID is needed")
  (let [existing (get-document (:_id (merge {:_id id} doc)))
        updated-doc (if (empty? doc)
                      ;;If document is empty - don't bother updating it
                      existing
                      (update-document (merge existing doc)))]
    (if (nil? file)
      updated-doc
      (put-attachment updated-doc file))))

(defn -main [& args]
  (let [[arguments extra-args banner] (cli args
                                           ["-db" "--database" "Database" :default ""]
                                           ["-m" "--method" "Method: get, save, update or delete" :default "get"]
                                           ["-id" "--document-id" "Document ID" :default ""]
                                           ["-d" "--document" "Document" :default "{}"]
                                           ["-f" "--file" "File"]
                                           ["-h" "--help" "Help" :flag true])]
    (with-db (authenticated-database (:database arguments))
             (if (:help arguments)
               (println banner))
             (let [doc (:document arguments)
                   json-doc (if doc (read-json doc))
                   id (:document-id arguments)
                   method (:method arguments)]
               (println
                 (case (keyword method)
                   :get (get-document id)
                   :save (put-document json-doc)
                   :update (update-doc id json-doc (:file arguments))
                   :delete (and (assert (not-empty json-doc) "Document is needed") 
                                (delete-document json-doc))
                   (str "Unknown method " method)))))))

