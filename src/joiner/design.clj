(ns joiner.design
  (:use [com.ashafa.clutch]
        [joiner.core]
        [joiner.resource])
  (:require [clojure.contrib.logging :as log]))

(defn- load-files [path key-names]
  (let [loader-fn (fn[sum key-name]
                    (let [file-content (load-resource (str path key-name ".js"))]
                      (if (nil? file-content)
                        sum
                        (assoc sum (keyword key-name) file-content))))]
    (reduce loader-fn {} key-names)))

(defn- reload-design-doc-element [design-doc element key-names directories]
  "Update the design document element with the content from the 
  files found under given directories."
  (let [ddoc (get-document (str "_design/" design-doc))
        new-element (reduce (fn [new-element dir]
                              (let [content (load-files (str design-doc "/" element "/" dir "/") key-names)]
                                (if (empty? content)
                                  (dissoc new-element (keyword dir))
                                  (assoc new-element (keyword dir) content))))
                            ((keyword element) ddoc) directories)]
    (if (nil? ddoc)
      (create-document {:_id (str "_design/" design-doc) (keyword element) new-element})
      (update-document (assoc ddoc (keyword element) new-element)))))


(defn update-fulltext [design-doc & indices]
  (reload-design-doc-element design-doc "fulltext" ["index"] indices))


(defn update-view [design-doc & view-names]
  "Create or update a new view based on the resources found at
  design-doc/view-name/[map.js reduce.js]"
  (reload-design-doc-element design-doc "views" ["map" "reduce"] view-names))


