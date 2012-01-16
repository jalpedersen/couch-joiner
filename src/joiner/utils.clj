(ns joiner.utils
  (:require [com.ashafa.clutch.http-client :as http])
  (:use [joiner.core] 
        [clojure.string :only [split]]
        [clojure.data.json :only [json-str]]))

(defmacro catch-couchdb-exceptions [& body]
  `(try
     ~@body
     (catch java.io.IOException e#
       (let [msg# (.getMessage e#)
             tokens# (split msg# #" " 5)]
         (if (and (= (count tokens#) 5) (= (.startsWith msg# "CouchDB Response Error:")))
           ;;Clutch error message is something like this: CouchDB Response Error: " response-code " " response-message 
           {:status (Integer/parseInt (get tokens# 3))
            :error (get tokens# 4)
            :message msg#}
           (throw e#))))))


(defn uuids 
  ([]
   (uuids 1))
  ([count]
   (:uuids (http/couchdb-request :get
                                 (database-url (str "_uuids?count=" count))))))
