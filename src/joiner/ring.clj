(ns joiner.ring
  (:require [com.ashafa.clutch.http-client :as http]
            [com.ashafa.clutch.utils :as utils])
  (:use [joiner.core]))

(defn- in-any-role? [allow-roles roles]
  (if (empty? allow-roles)
    false
    (or (contains? roles (first allow-roles))
        (recur (rest allow-roles) roles))))

(defn- get-current-user [request]
    "Authenticate against the configured couchdb instance using the given ring request's headers"
    (:userCtx (http/couchdb-request :get (utils/url (couchdb-instance) "_session") :headers (:headers request))))

(defn- get-user [request]
  (if (:username request)
    {:name (:username request)
     :roles (:roles request)}
    (get-current-user request)))


(defn wrap-couchdb-user [handler & [ & {:keys [allow-roles]}]]
  "Wrap request with current couchdb user.
  If a user context is available, request is associated with
  a user (:username) and a predicate that given a role name returns true
  or false depending on whether user is in given role or not."
  (let [error-response {:status 403
                        :headers {"content-type" "text/plain"}
                        :body "Not authorized."}]
    (fn [request]
      (let [user (get-user request)]
        (if (and (not (empty? allow-roles))
                 (not (in-any-role? allow-roles (set (:roles user)))))
          error-response
          (handler (assoc request
                          :username (:name user)
                          :roles (:roles user)
                          :in-role? (fn [role]
                                      (contains? (set (:roles user)) role)))))))))
