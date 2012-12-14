(ns joiner.user
  (:require [clojure.string :as s]
            [cemerick.url :as url]
            [clojure.data.codec.base64 :as b64]
            [com.ashafa.clutch :as clutch]
            [com.ashafa.clutch.http-client :as http])
  (:use [joiner.core]
        [joiner.admin]))

(def ^:private ^:dynamic *users-db* "_users")

(defn- get-uuid []
  (first
    (:uuids (http/couchdb-request :get
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
  (clutch/with-db (authenticated-database *users-db*)
                  (clutch/put-document
                    {:_id (str "org.couchdb.user:" username)
                     :name username
                     :password password
                     :type "user"
                     :roles roles})))

;;Sha-1 encoding of password was only required prior to 1.2.0
;;(defn create-user [username, password, roles]
;; "Create a new user with given password and roles."
;; (let [salt (get-uuid)]
;;    (with-db (authenticated-database *users-db*)
;;             (put-document
;;               {:_id (str "org.couchdb.user:" username)
;;                :name username
;;                password_sha (sha1 password salt)
;;                salt salt
;;                :password password
;;                :type "user"
;;                :roles roles}))))


(defn get-user [username]
  "Get user from username"
  (clutch/with-db (authenticated-database *users-db*)
                  (clutch/get-document (str "org.couchdb.user:" username))))

(defn- urlencode [string]
  (java.net.URLEncoder/encode string "UTF-8"))

(defn authenticate [username, password]
  "Seems like something is broken somewhere between here and clj-http for usernames
  containing special characters (such as @) - hence these hoops"
  (try
    (let [url (url/url (assoc (couchdb-instance) :username nil :password nil) "_session")
          ^bytes auth-bytes (b64/encode (.getBytes (str username ":" password)))
          authorization (String. auth-bytes)
          headers {"Authorization" (str "Basic " authorization)}]
      (let [resp (http/couchdb-request :get url :headers headers)]
        (and (:ok resp)
             (not (nil? (:name (:userCtx resp)))))))
    (catch clojure.lang.ExceptionInfo e
      false)))

(defn set-password [username, password]
  "Set new password for user"
  (let [user (get-user username)]
    (clutch/with-db (authenticated-database *users-db*)
                    (clutch/update-document (assoc user
                                                   :password password)))))

;;(defn set-password [username, password]
;;  "Set new password for user"
;;  (let [salt (get-uuid)
;;        user (get-user username)]
;;    (with-db (authenticated-database *users-db*)
;;             (update-document (assoc user
;;                                     :salt salt
;;                                     :password_sha (sha1 password salt))))))

(defn update-user [user]
  "Update user along. Typically used when updating roles
  and other meta-data"
  (clutch/with-db (authenticated-database *users-db*)
                  (clutch/update-document user)))

(defn delete-user [user]
  (clutch/with-db (authenticated-database *users-db*)
                  (clutch/delete-document user)))

(defn delete-private-database [user]
  (if-let [db (:userdb user)]
    (do
      (clutch/delete-database (authenticated-database db))
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
    (try (clutch/create-database (database-url db-name))
      (try (do (clutch/with-db (authenticated-database db-name) (security  acl))
             (update-user (assoc user :userdb db-name)))
        ;;Should we remove the database
        ;;if we cannot update the user data?
        (catch Exception _ 
          (clutch/delete-database (database-url db-name))))
      (catch java.io.IOException _
        (create-private-database user
                                 (if (nil? postfix)
                                   0
                                   (+ postfix 1)))))))


