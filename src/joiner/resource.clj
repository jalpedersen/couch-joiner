(ns joiner.resource
  (:use clojure.java.io))

(defn- load-file-or-resource [name load-fn]
  "Load file and pass the stream to load-fn"
  (let [file (file name)
        do-load (fn [open-fn]
                  (with-open [stream (open-fn)]
                    (load-fn stream)))]
    (if (.isFile file)
      (do-load (fn [] (input-stream file)))
      (let [url (resource name)]
        (if url
          (do-load (fn [] (.openStream url))))))))


(defn load-properties [name]
  "Load named property first from file then from resource"
  (let [p (load-file-or-resource name #(doto (java.util.Properties.)
                     (.load %1)))]
    (if p
      p
      (java.util.Properties.))))

(defn load-resource [name]
  "Load file first from file relative from current
  directory, then from a resource."
  (load-file-or-resource name #(slurp %1)))
