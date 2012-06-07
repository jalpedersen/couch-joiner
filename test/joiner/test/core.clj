(ns joiner.test.core
  (:require [joiner.core :as core]
            [joiner.admin :as admin]
            [joiner.design :as design]
            [joiner.user :as user]
            [joiner.resource :as resource]
            [joiner.utils :as utils]
            [joiner.search :as search]
            [com.ashafa.clutch :as clutch]
            [com.ashafa.clutch.http-client :as http]
            [joiner.main] :reload-all)
  (:use clojure.test))

(def testdb "joiner_testdb")

(defmacro with-test-db
  [body]
  `(let [db# (clutch/get-database (core/authenticated-database testdb))]
     (try 
       (~@body)
       (finally (clutch/delete-database db#)))))

(deftest test-set-security
         (let [acl {:admins {:roles ["important"]}
                    :readers {:names ["joe"]}}]
           (with-test-db
             (do
               (clutch/with-db (core/authenticated-database testdb)
                        (is (:ok (admin/security acl)))
                        (is (= acl (admin/security))))))))
(deftest test-error-handling
         (with-test-db
           (let [response (utils/catch-couchdb-exceptions
                                 (clutch/with-db (core/authenticated-database "_bad-name")
                                          (http/couchdb-request :get (core/authenticated-database testdb))))]
             (is (= 400 (:status response)))
             (is (.equals "Bad Request" (:error response))))))

(deftest test-design
         (with-test-db
           (clutch/with-db (core/authenticated-database testdb)
                    (do
                      (design/update-views "testing" "test-view")
                      (design/update-views "testing" "test-view" "another-view")))))
