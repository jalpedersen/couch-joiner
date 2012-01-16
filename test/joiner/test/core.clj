(ns joiner.test.core
  (:use joiner.core
        joiner.admin
        joiner.design
        joiner.user
        joiner.resource
        joiner.utils
        joiner.search
        com.ashafa.clutch
        com.ashafa.clutch.http-client
        joiner.main :reload-all)
  (:use clojure.test))

(def testdb "joiner_testdb")

(defmacro with-test-db
  [body]
  `(let [db# (get-database (authenticated-database testdb))]
     (try 
       (~@body)
       (finally (delete-database db#)))))

(deftest test-set-security
         (let [acl {:admins {:roles ["important"]}
                    :readers {:names ["joe"]}}]
           (with-test-db
             (do
               (with-db (authenticated-database testdb)
                        (is (:ok (security acl)))
                        (is (= acl (security))))))))
(deftest test-error-handling
         (with-test-db
           (let [response (catch-couchdb-exceptions
                                 (with-db (authenticated-database "_bad-name")
                                          (couchdb-request :get (authenticated-database testdb))))]
             (is (= 400 (:status response)))
             (is (.equals "Bad Request" (:error response))))))

(deftest test-design
         (with-test-db
           (with-db (authenticated-database testdb)
                    (do
                      (update-views "testing" "test-view")
                      (update-views "testing" "test-view" "another-view")))))
