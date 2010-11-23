(ns joiner.design
  (:use com.ashafa.clutch)
  (:use joiner.core)
  (:use joiner.resource))

(defn- load-view [path]
  (let [loader-fn (fn[sum value]
		    (let [file-content (load-resource (str path value ".js"))]
		      (if (nil? file-content)
			    sum
			    (assoc sum (keyword value) file-content))))]
    (reduce loader-fn {} ["map" "reduce"])))
  

(defn update-view [database design-doc view-name]
  "Create or update a new view based on the resources found at
   design-doc/view-name/[map.js reduce.js]"
  (with-db database database
    (save-view design-doc (keyword view-name)
	       (load-view (str design-doc "/views/" view-name "/")))))

