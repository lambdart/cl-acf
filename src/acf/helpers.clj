(ns acf.helpers
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace
                                    lower-case]]))

(defn compact-map
  "Returns a `map` with all entries removed, where the entrie's value
  matches `pred`."
  [map & [pred]]
  (let [pred (or pred nil?)]
    (reduce
     (fn [m k]
       (let [value (get map k)]
         (if (pred value) m (assoc m k value))))
     {}
     (keys map))))

(defn coerce
  "Coerce the string `s` to the type of `obj`."
  [obj s]
  (cond
    (keyword? obj) (keyword s)
    (symbol? obj) (symbol s)
    :else s))

(defn str-name
  "Same as `clojure.core/name`, but keeps the namespace for keywords
  and symbols."
  [x]
  (cond
    (nil? x) x
    (string? x) x
    (or (keyword? x)
        (symbol? x)) (if-let [ns (namespace x)]
                       (str ns "/" (name x))
                       (name x))))

(defn hyphenate
  "Hyphenate x, which is the same as threading `x` through the str,
  underscore and dasherize fns."
  [x]
  (when x
    (->> (-> (str-name x)
             (replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
             (replace #"([a-z\d])([A-Z])" "$1-$2")
             (replace #"\s+" "-")
             (replace #"_" "-")
             (lower-case))
         (coerce x))))

(defn underscore
  "Makes an underscored, lowercase form from the expression in the string
  `x`."
  [x]
  (when x
    (->> (-> (str-name x)
             (replace #"([A-Z\d]+)([A-Z][a-z])" "$1_$2")
             (replace #"([a-z\d])([A-Z])" "$1_$2")
             (replace #"-" "_")
             lower-case)
         (coerce x))))

(defn transform-keys
  "Recursively transform all keys in the map `m` by applying `f` on them."
  [m f]
  (if (map? m)
    (reduce
     (fn [acc key]
       (let [value (get m key)]
         (-> (dissoc acc key)
             (assoc (f key)
                    (cond
                      (map? value) (transform-keys value f)
                      (vector? value) (mapv #(transform-keys % f) value)
                      (sequential? value) (map #(transform-keys % f) value)
                      :else value)))))
     m
     (keys m))
    m))

(defn transform-values
  "Recursively transform all map values of m by applying f on them."
  [m f]
  (if (map? m)
    (reduce
     (fn [acc key]
       (let [value (get m key)]
         (assoc acc key (if (map? value)
                          (transform-values value f)
                          (f value)))))
     m
     (keys m))
    m))

(defn hyphenate-keys
  "Recursively apply hyphenate on all keys of m."
  [m]
  (-> m (transform-keys hyphenate)))

(defn hyphenate-values
  "Recursively apply hyphenate on all values of m."
  [m]
  (-> m (transform-values hyphenate)))
