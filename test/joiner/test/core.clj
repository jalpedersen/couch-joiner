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
	(is (:ok (security *testdb* acl)))
	(is (= acl (security *testdb*)))))))
