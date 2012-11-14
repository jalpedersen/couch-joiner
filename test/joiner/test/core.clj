(ns joiner.test.core
  (:require [joiner.core :as core]
            [joiner.admin :as admin]
            [joiner.design :as design]
            [joiner.user :as user]
            [joiner.resource :as resource]
            [joiner.utils :as utils]
            [joiner.search :as search]
            [joiner.ring :as ring]
            [com.ashafa.clutch :as clutch]
            [com.ashafa.clutch.http-client :as http]
            [cheshire.core :as json]
            [joiner.main] :reload-all)
  (:use clojure.test))

(def testdb "joiner_testdb")

(defmacro with-test-db
  [body]
  `(let [db# (clutch/get-database (core/authenticated-database testdb))]
     (try 
       (~@body)
       (finally (clutch/delete-database db#)))))

(deftest test-create-user
         (if (user/get-user "test_user42")
           (user/delete-user (user/get-user "test_user42")))
         (is (= "test_user42"  (:name (user/create-user "test_user42" "123" ["user"]))))
         (is (user/authenticate "test_user42" "123"))
         (is (false? (user/authenticate "test_user42" "1234")))
         (is (= "test_user42" (:name (user/set-password "test_user42" "1234"))))
         (is (user/authenticate "test_user42" "1234"))
         (user/delete-user (user/get-user "test_user42")))


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
             (let [json-resp (json/parse-string (:body response) true)]
               (is (= "illegal_database_name" (:error json-resp)))))))

(deftest test-design
         (with-test-db
           (clutch/with-db (core/authenticated-database testdb)
                    (do
                      (design/update-views "testing" "test-view")
                      (design/update-views "testing" "test-view" "another-view")))))
