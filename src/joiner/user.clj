(ns joiner.user
  (:use joiner.core)
  (:use com.ashafa.clutch)
  (:use com.ashafa.clutch.http-client)
  (:use [clojure.contrib.string
	 :only (lower-case replace-re)]))

(def *users-db* "_users")

(defn- get-uuid []
  (first
   (:uuids (couchdb-request (get-secure-database "_uuids")
			    :get))))

(defn- to-hex [byte]
  (let [val  (Integer/toString (bit-and byte 0xff) 16)]
    (if (= (.length val) 1)
      (str "0" val)
      val)))

  
(defn- sha1 [string salt]
  (let [bytes (.getBytes (str string salt) "UTF-8")
	digester (java.security.MessageDigest/getInstance "SHA1")] 
    (apply str (map to-hex (.digest digester bytes)))))

;;Create a new user with given roles
(defn create-user [username, password, roles]
  (let [salt (get-uuid)]
    (with-db (get-secure-database *users-db*)
      (create-document
       {:_id (str "org.couchdb.user:" username)
	:name username
	:password_sha (sha1 password salt)
	:salt salt
	:type "user"
	:roles roles
       }))
    ))

;;Get user from username
(defn get-user [username]
  (with-db (get-secure-database *users-db*)
    (get-document (str "org.couchdb.user:" username))))

;;Set new password for user
(defn set-password [username, password]
  (let [salt (get-uuid)
	user (get-user username)]
    (with-db (get-secure-database *users-db*)
      (update-document (assoc user
			 :salt salt
			 :password_sha (sha1 password salt))))))

;;Update user along. Typically used when updating roles
;;and other meta-data
(defn update-user [user]
  (with-db (get-secure-database *users-db*)
     (update-document user)))

;;Set roles for user
(defn set-roles [username, roles]
  (let [user (get-user username)]
    (update-user (assoc user :roles roles))))

;;Get security settings for database
(defn get-security [db-name]
  (couchdb-request (get-secure-database db-name)
		   :get
		   :command "_security"))

;;Set security settings for database
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
  (couchdb-request (get-secure-database db-name)
		   :put
		   :command "_security"
		   :data security-settings))
  
(defn- clean-name [name]
  (replace-re #"[^a-z0-9\\+]" "\\_" (lower-case name)))

;;Create new database for user and store the name
;;of the new database in the users meta-data
(defn create-private-database [user & [postfix]]
  ;;Database name may only contain:
  ;;lower case letters, numbers,  _, $, (, ), +, -, and /
  (let [clean-name (clean-name (:name user))
	db-name (str "userdb_"
		     (if (nil? postfix)
		       clean-name
		       (str clean-name "_" postfix)))
	name {:names [(:name user)]}
	acl {:admins name :readers name}]
    (try (create-database (get-secure-database db-name))
	 (try (do (set-security db-name acl)
		  (update-user (assoc user :userdb db-name)))
	      ;;Should we remove the database
	      ;;if we cannot update the user data?
	      (catch Exception _))
	 (catch java.io.IOException _
	   (create-private-database user
				 (if (nil? postfix)
				   0
				   (+ postfix 1)))))))

(defn create-admin [username])

