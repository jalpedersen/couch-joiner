(ns joiner.search
  (:require [com.ashafa.clutch.http-client :as http])
  (:use [joiner.core]
        [com.ashafa.clutch]))

(defn- lucene-request [method design-doc-id index query]
  (let [db (get-database)
        db-name (:path db)
        fti-key (or (:fti-key db) "local")
        fti-prefix (or (:fti-prefix db) "_fti")]
    (http/couchdb-request method
                          (assoc db
                                 :path (str fti-prefix "/" fti-key "/" db-name "/" design-doc-id (if (not (nil? index)) (str "/" index)))
                                 :query query))))


(defn search [design-doc-name index query & options]
  {:pre [(string? query)]}
  (lucene-request :get (str "_design/" design-doc-name) (name index) (assoc (apply hash-map options) :q query)))

(defn index-info [design-doc-name index]
  (lucene-request :get (str "_design/" design-doc-name) (name index) nil))

(defn optimize-index [design-doc-name index]
  (lucene-request :post (str "_design/" design-doc-name) (str (name index) "/_optimize") nil))

(defn expunge-index [design-doc-name index]
  (lucene-request :post (str "_design/" design-doc-name) (str (name index) "/_expunge") nil))

(defn cleanup-index []
  (lucene-request :post "_cleanup" nil nil))
