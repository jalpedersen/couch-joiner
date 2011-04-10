(ns joiner.core
  (:use com.ashafa.clutch)
  (:use com.ashafa.clutch.http-client)
  (:use joiner.resource))

(defn- get-properties []
  (let [filename (System/getProperty "joiner-conf" "joiner.properties")]
    (load-properties filename)))


(defn get-secure-database [name]
  "Authenticated access to database"
  (let [prop-set (.entrySet (get-properties))
	prop-keys (map (fn [e] (keyword (key e))) prop-set)
	prop-values (map (fn [e] (.getValue e)) prop-set)]
    (get-database (assoc (zipmap prop-keys prop-values)
		    :name name
		    :language "javascript"))))


(defn get-security [db-name]
  "Get security settings for database"
  (couchdb-request (get-secure-database db-name)
		   :get
		   :command "_security"))

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
(defn set-security [db-name security-settings]
  "Set security settings for database"
  (couchdb-request (get-secure-database db-name)
		   :put
		   :command "_security"
		   :data security-settings))
  
