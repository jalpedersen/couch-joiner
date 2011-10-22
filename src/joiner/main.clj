(ns joiner.main
  (:gen-class)
  (:use [com.ashafa.clutch]
        [joiner.core]
        [joiner.user]
        [clojure.data.json]
        [clojure.tools.cli]))


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
  (let [arguments (cli args
                       (required ["-db" "--database" "Database"])
                       (required ["-op" "--operation" "Operation: get, save, update or delete"])
                       (optional ["-id" "--document-id" "Document ID"])
                       (optional ["-d" "--document" "Document" :default "{}"])
                       (optional ["-f" "--file" "File"]))]
    (with-db (authenticated-database (:database arguments))
             (let [doc (:document arguments)
                   json-doc (if doc (read-json doc))
                   id (:document-id arguments)
                   op (:operation arguments)]
               (println
                 (case (keyword op)
                   :get (get-document id)
                   :save (create-document json-doc)
                   :update (update-doc id json-doc (:file arguments))
                   :delete (delete-document json-doc)
                   (str "Unknown operation " op)))))))



