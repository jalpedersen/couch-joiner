(ns joiner.core
  (:require [com.ashafa.clutch.utils :as utils]
            [com.ashafa.clutch :as clutch])
  (:use [joiner.resource]))

(defn- load-auth-properties []
  (let [filename (System/getProperty "joiner-conf" "joiner.properties")
        ^java.util.Properties properties (load-properties filename)]
    (loop [props {} prop-set (.entrySet properties)]
      (if (seq prop-set)
        (let [entry (first prop-set)
              key (keyword (.getKey entry))
              value (.getValue entry)]
          (recur (assoc props key value) (next prop-set)))
        props))))

;;Initialise properties
(def ^{:private true} autentication-props (atom nil))
(def ^{:private true} valid-properties [:username :password :fti-key :fti-prefix])

(defn reload-properties []
  (reset! autentication-props nil))

(defn- db-properties []
  (if (nil? @autentication-props)
    (let [properties (load-auth-properties)
          connection (merge (utils/url (:url properties))
                            (select-keys properties valid-properties))]
      (reset! autentication-props connection))
    @autentication-props))

(defn couchdb-instance []
  (db-properties))

(defn database-url [name]
  (utils/url (db-properties) name))

(defn authenticated-database [name]
  "Authenticated access to database"
  (clutch/get-database-with-db (database-url name)))

(defmacro with-authenticated-db [db-name & body]
  `(clutch/with-db (authenticated-database ~db-name)
     (do
       ~@body)))

