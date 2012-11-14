(ns joiner.main
  (:gen-class)
  (:use [joiner.core]
        [joiner.user])
  (:require [cheshire.core :as json]
            [com.ashafa.clutch :as clutch]
            [clojure.tools.cli :as cli]))


(defn- update-doc [id doc file]
  (assert (not-empty id) "Document ID is needed")
  (let [existing (clutch/get-document (:_id (merge {:_id id} doc)))
        updated-doc (if (empty? doc)
                      ;;If document is empty - don't bother updating it
                      existing
                      (clutch/update-document (merge existing doc)))]
    (if (nil? file)
      updated-doc
      (clutch/put-attachment updated-doc file))))

(defn -main [& args]
  (let [[arguments extra-args banner] (cli/cli args
                                               ["-db" "--database" "Database" :default ""]
                                               ["-m" "--method" "Method: get, save, update or delete" :default "get"]
                                               ["-id" "--document-id" "Document ID" :default ""]
                                               ["-d" "--document" "Document" :default "{}"]
                                               ["-f" "--file" "File"]
                                               ["-h" "--help" "Help" :flag true])]
    (clutch/with-db (authenticated-database (:database arguments))
                    (if (:help arguments)
                      (println banner))
                    (let [doc (:document arguments)
                          json-doc (if doc (json/parse-string doc))
                          id (:document-id arguments)
                          method (:method arguments)]
                      (println
                        (case (keyword method)
                          :get (clutch/get-document id)
                          :save (clutch/put-document json-doc)
                          :update (update-doc id json-doc (:file arguments))
                          :delete (and (assert (not-empty json-doc) "Document is needed")
                                       (clutch/delete-document json-doc))
                          (str "Unknown method " method)))))))

