(ns joiner.user
  (:require [clojure.string :as s])
  (:use [joiner.core]
        [joiner.admin]
        [com.ashafa.clutch]
        [com.ashafa.clutch.http-client]))

(def ^:dynamic *users-db* "_users")

(defn- get-uuid []
  (first
    (:uuids (couchdb-request :get
                             (database-url "_uuids")))))

(defn- to-hex [byte]
  (let [clean-byte (bit-and byte 0xff)
        val  (Integer/toString clean-byte 16)]
    (if (< clean-byte 0x10)
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
             (put-document
               {:_id (str "org.couchdb.user:" username)
                :name username
                :password_sha (sha1 password salt)
                :salt salt
                :type "user"
                :roles roles}))))

(defn get-user [username]
  "Get user from username"
  (with-db (authenticated-database *users-db*)
           (get-document (str "org.couchdb.user:" username))))

(defn authenticate [username, password]
  (let [user (get-user username)]
    (if user
      (= (:password_sha user) (sha1 password (:salt user))))))

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

(defn delete-user [user]
  (with-db (authenticated-database *users-db*)
           (delete-document user)))

(defn delete-private-database [user]
  (if-let [db (:userdb user)]
    (do
      (delete-database (authenticated-database db))
      (update-user (dissoc user :userdb)))))

(defn set-roles [username, roles]
  "Set roles for user"
  (let [user (get-user username)]
    (update-user (assoc user :roles roles))))

(defn- clean-name [name]
  (s/replace (s/lower-case name) #"[^a-z0-9\\+]" "\\_" ))

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
    (try (create-database (database-url db-name))
      (try (do (with-db (authenticated-database db-name) (security  acl))
             (update-user (assoc user :userdb db-name)))
        ;;Should we remove the database
        ;;if we cannot update the user data?
        (catch Exception _ 
          (delete-database (database-url db-name))))
      (catch java.io.IOException _
        (create-private-database user
                                 (if (nil? postfix)
                                   0
                                   (+ postfix 1)))))))

