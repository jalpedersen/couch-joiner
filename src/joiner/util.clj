(ns joiner.util
  (:use [clojure.string :only [split]]
        [clojure.contrib.json :only [json-str]]))

(defmacro catch-couchdb-exceptions [& body]
  `(try
     ~@body
     (catch java.io.IOException e#
       (let [msg# (.getMessage e#)
             tokens# (split msg# #" " 5)]
         (if (and (= (count tokens#) 5) (= (.startsWith msg# "CouchDB Response Error:")))
           ;;Clutch error message is something like this: CouchDB Response Error: " response-code " " response-message 
           {:status (Integer/parseInt (get tokens# 3))
            :error (get tokens# 4)
            :message msg#}
           (throw e#))))))
