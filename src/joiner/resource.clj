(ns joiner.resource
  (:use clojure.java.io))

(defn load-properties [name]
  "Load named property first from file then from resource"
  (let [file (java.io.File. name)
	do-load (fn [open-fn]
		  (with-open [stream (open-fn)]
		    (doto (java.util.Properties.)
		      (.load stream))))]
    (if (.isFile file)
      (do-load (fn [] (java.io.FileInputStream. file)))
      (let [url (resource name)]
	(if (nil? url)
	  (java.util.Properties.)
	  (do-load (fn [] (.openStream url))))))))

(defn load-resource [name]
  "Load file first from file relative from current
directory, then from a resource."
  (let [file (java.io.File. name)
	do-load (fn [open-fn]
		  (with-open [stream (open-fn)]
		    (slurp stream)))]
    (if (.isFile file)
      (do-load (fn [] (java.io.FileInputStream. file)))
      (let [url (resource name)]
	(if (nil? url)
	  nil
	  (do-load (fn [] (.openStream url))))))))
