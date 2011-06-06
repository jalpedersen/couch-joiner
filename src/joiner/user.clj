(ns joiner.user
  (:use [joiner.core]
        [com.ashafa.clutch]
        [com.ashafa.clutch.http-client]
        [clojure.contrib.string :only (lower-case replace-re)]))

(def *users-db* "_users")

(defn- get-uuid []
  (first
   (:uuids (couchdb-request (authenticated-database "_uuids")
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


(defn create-user [username, password, roles]
  "Create a new user with given password and roles."
  (let [salt (get-uuid)]
    (with-db (authenticated-database *users-db*)
      (create-document
       {:_id (str "org.couchdb.user:" username)
	:name username
	:password_sha (sha1 password salt)
	:salt salt
	:type "user"
	:roles roles
       }))
    ))

(defn get-user [username]
  "Get user from username"
  (with-db (authenticated-database *users-db*)
    (get-document (str "org.couchdb.user:" username))))


(defn set-password [username, password]
  "Set new password for user"
  (let [salt (get-uuid)
	user (get-user username)]
    (with-db (authenticated-database *users-db*)
      (update-document (assoc user
			 :salt salt
			 :password_sha (sha1 password salt))))))

(defn update-user [user]
  "Update user along. Typically used when updating roles
and other meta-data"
  (with-db (authenticated-database *users-db*)
     (update-document user)))


(defn set-roles [username, roles]
  "Set roles for user"
  (let [user (get-user username)]
    (update-user (assoc user :roles roles))))

(defn- clean-name [name]
  (replace-re #"[^a-z0-9\\+]" "\\_" (lower-case name)))

(defn create-private-database [user & [postfix]]
  "Create new database for user and store the name
of the new database in the users meta-data"
  ;;Database name may only contain:
  ;;lower case letters, numbers,  _, $, (, ), +, -, and /
  (let [clean-name (clean-name (:name user))
	db-name (str "userdb_"
		     (if (nil? postfix)
		       clean-name
		       (str clean-name "_" postfix)))
	name {:names [(:name user)]}
	acl {:admins name :readers name}]
    (try (create-database (db-auth db-name))
	 (try (do (with-db (authenticated-database db-name) (security  acl))
		  (update-user (assoc user :userdb db-name)))
	      ;;Should we remove the database
	      ;;if we cannot update the user data?
	      (catch Exception _ 
                (delete-database (db-auth db-name))))
	 (catch java.io.IOException _
	   (create-private-database user
				 (if (nil? postfix)
				   0
				   (+ postfix 1)))))))

(defn create-admin [username])

