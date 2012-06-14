(ns joiner.admin
  (:require [com.ashafa.clutch.utils :as utils]
            [com.ashafa.clutch.http-client :as http])
  (:use [joiner.core]
        [com.ashafa.clutch :only (with-db get-document get-database)]))

(defn create-admin [username password]
  (http/couchdb-request :put
                        (database-url (str "_config/admins/" username))
                        :data-type "text/plain"
                        :data (str "\"" password "\"")))
(defn delete-admin [username]
  (http/couchdb-request :delete
                        (database-url (str "_config/admins/" username))))
(defn get-admins []
  (http/couchdb-request :get
                        (database-url "_config/admins/")))

(defn get-configuration []
  (http/couchdb-request :get
                        (database-url "_config")))

(defn- get-security []
  "Get security settings for current database"
  (get-document "_security"))

(defn- set-security [security-settings]
  "Set security settings for current database"
  (http/couchdb-request :put
                        (utils/url (get-database) "_security")
                        :data security-settings))

;;Example settings:
;;{
;;  "admins": {
;;    "roles": ["local-heroes"],
;;    "names": ["rebecca", "pete"]
;;  },
;;  "readers": {
;;    "roles": ["lolcat-heroes"],
;;    "names": ["simon", "ben", "james"]
;;  }
;;}
(defn security
  "Get or set security settings for current database"
  ([]
   (get-security))
  ([settings]
   (set-security settings)))

(defn compact [& [db]]
  (let [database (if (nil? db) (get-database) db)]
    (http/couchdb-request :post
                          (utils/url db "_compact"))))

