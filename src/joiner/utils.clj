(ns joiner.utils
  (:require [com.ashafa.clutch.http-client :as http]
            [com.ashafa.clutch.utils :as utils])
  (:use [joiner.core]))

(defmacro catch-couchdb-exceptions [& body]
  `(try
     ~@body
     (catch clojure.lang.ExceptionInfo e#
       (let [data# (:object (ex-data e#))]
         (select-keys data# [:status :body :headers])))))

(defn uuids 
  ([]
   (uuids 1))
  ([count]
   (:uuids (http/couchdb-request :get
                                 (database-url (str "_uuids?count=" count))))))
