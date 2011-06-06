(ns joiner.test.core
  (:use joiner.core
	joiner.design
	joiner.user
	joiner.resource
	com.ashafa.clutch
	joiner.main :reload-all)
  (:use clojure.test))

(def *testdb* "joiner_testdb")

(defmacro with-test-db
  [body]
  `(let [db# (get-database (authenticated-database *testdb*))]
     (try 
       (~@body)
       (finally (delete-database db#)))))

(deftest test-set-security
  (let [acl {:admins {:roles ["important"]}
	     :readers {:names ["joe"]}}]
    (with-test-db
      (do
        (with-db (authenticated-database *testdb*)
	  (is (:ok (security acl)))
	  (is (= acl (security))))))))

(deftest test-design
  (with-test-db
    (with-db (authenticated-database *testdb*)
      (do
        (update-view "testing" "test-view")
        (update-view "testing" "test-view" "another-view")))))
