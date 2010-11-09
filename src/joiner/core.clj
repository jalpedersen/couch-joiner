(ns joiner.core
  (:use com.ashafa.clutch)
  (:use joiner.properties))

(defn- get-properties []
  (let [filename (System/getProperty "joiner-conf" "joiner.properties")]
    (load-properties filename)))

;;Authenticated access to database
(defn get-secure-database [name]
  (let [prop-set (.entrySet (get-properties))
	prop-keys (map (fn [e] (keyword (key e))) prop-set)
	prop-values (map (fn [e] (.getValue e)) prop-set)]
    (assoc (zipmap prop-keys prop-values)
      :name name
      :language "javascript")))


