(ns joiner.design
  (:use [com.ashafa.clutch]
        [joiner.core]
        [joiner.resource])
  (:require [clojure.tools.logging :as log]))

(defn- load-files [path key-names]
  (let [loader-fn (fn[sum key-name]
                    (let [file-content (load-resource (str path key-name ".js"))]
                      (if (nil? file-content)
                        sum
                        (assoc sum (keyword key-name) file-content))))]
    (if key-names
      (reduce loader-fn {} key-names)
      (loader-fn {} ""))))

(defn- load-element [design-doc name keys sub-elements]
  "load files for a design doc element. For instance a view:
  views [map reduce] by-id by-contact "
  (reduce (fn [new-element dir]
            (let [content (load-files (str design-doc "/" name "/" dir (if keys "/")) keys)]
              (if (empty? content)
                (dissoc new-element (keyword dir))
                (assoc new-element (keyword dir) content))))
          {} sub-elements))

(defn- reload-design-doc-element [design-doc element sub-elements key-names]
  "Update the design document element with the content from the 
  files found under given sub-elements."
  (let [ddoc (get-document (str "_design/" design-doc))
        new-element (load-element design-doc element key-names sub-elements)]
    (if (nil? ddoc)
      (put-document {:_id (str "_design/" design-doc) (keyword element) new-element})
      (update-document (assoc ddoc (keyword element) new-element)))))

(defn update-fulltext [design-doc & indices]
  (reload-design-doc-element design-doc "fulltext" indices ["index"]))

(defn update-views [design-doc & view-names]
  "Create or update a new view based on the resources found at
  design-doc/view-name/[map.js reduce.js]"
  (reload-design-doc-element design-doc "views" view-names ["map" "reduce"]))

(defn update-filters [design-doc & filters]
  (reload-design-doc-element design-doc "filters" filters nil))

