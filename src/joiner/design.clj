(ns joiner.design
  (:use [com.ashafa.clutch]
        [joiner.core]
        [joiner.resource])
  (:require [clojure.contrib.logging :as log]))

(defn- load-files [path files]
  (let [loader-fn (fn[sum value]
		    (let [file-content (load-resource (str path value ".js"))]
		      (if (nil? file-content)
			    sum
			    (assoc sum (keyword value) file-content))))]
    (reduce loader-fn {} files)))

(defn- remove-view [database design-doc view-name]
  (with-db database
    (let [doc (get-document (str "_design/" design-doc))
	  views (:views doc)]
      (update-document (assoc doc :views (dissoc views (keyword view-name)))))))

(defn update-view [database design-doc & view-names]
  "Create or update a new view based on the resources found at
   design-doc/view-name/[map.js reduce.js]"
  (loop [views view-names]
    (when (seq views)
      (let [view-name (first views)
            mapreduce (load-files (str design-doc "/views/" view-name "/")
                                   ["map" "reduce"])]
        (if (empty? mapreduce)
          (remove-view database design-doc view-name)
          ;;Should really update design document in one go
          (with-db database database
	    (save-view design-doc (keyword view-name) mapreduce))))
      (recur (next views)))))
