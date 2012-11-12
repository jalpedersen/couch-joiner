(ns joiner.utils
  (:require [com.ashafa.clutch.http-client :as http])
  (:use [joiner.core] 
        [clojure.string :only [split]]))

(defmacro catch-couchdb-exceptions [& body]
  `(try
     ~@body
     (catch clojure.lang.ExceptionInfo e#
       (let [data# (:object (ex-data e#))]
         {:status (:status data#)
          :body (:body data#)
          :headers (:headers data#)}))))


(defn uuids 
  ([]
   (uuids 1))
  ([count]
   (:uuids (http/couchdb-request :get
                                 (database-url (str "_uuids?count=" count))))))
