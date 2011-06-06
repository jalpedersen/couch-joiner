(ns joiner.core
  (:use [com.ashafa.clutch]
        [com.ashafa.clutch.http-client]
        [joiner.resource]))

(defn- load-auth-properties []
  (let [filename (System/getProperty "joiner-conf" "joiner.properties")
        properties (load-properties filename)]
    (loop [props {} prop-set (.entrySet properties)]
      (if (seq prop-set)
        (let [entry (first prop-set)
              key (keyword (.getKey entry))
              value (.getValue entry)]
          (recur (assoc props key value) (next prop-set)))
        props))))

;;Initialise properties
(def *autentication-props* (atom nil))

(defn reload-properties []
  (reset! *autentication-props* (load-auth-properties)))

(defn- get-properties[]
  (if (nil? @*autentication-props*)
    (do
      (reset! *autentication-props* (load-auth-properties))
      @*autentication-props*)
    @*autentication-props*))


(defn authenticated-database [name]
  "Authenticated access to database"
  (get-database (assoc (get-properties)
	         :name name
	         :language "javascript")))

(defn db-auth [name]
  (assoc (get-properties)
         :name name
         :language "javascript"))
 

(defn- get-security []
  "Get security settings for database"
  (couchdb-request config
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
(defn- set-security [security-settings]
  "Set security settings for database"
  (couchdb-request config
		   :put
		   :command "_security"
		   :data security-settings))
  
(defn security [& settings] 
  (if (nil? settings)
    (get-security)
    (set-security (first (merge settings)))))

