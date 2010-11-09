(ns joiner.properties
  (:use clojure.java.io))

;;Try loading property first from file then from resource
(defn load-properties [name]
  (let [file (java.io.File. name)
	do-load (fn [open-fn]
		  (with-open [ stream (open-fn)]
		    (doto (java.util.Properties.)
		      (.load stream))))]
    (if (.isFile file)
      (do-load (fn [] (java.io.FileInputStream. file)))
      (let [url (resource name)]
	(if (nil? url)
	  (java.util.Properties.)
	  (do-load (fn [] (.openStream url))))))))
