(ns acf.util
  (:refer-clojure :exclude [replace])
  (:require [clj-http.client :as http]
            [clj-http.util :as http.util]
            [acf.sec :as sec]
            [acf.digest :as digest]
            [acf.helpers :refer :all]
            [clojure.string :refer [replace split]]))

(defn content-type
  "Returns the content type of `response`."
  [response]
  (first (split (get (:headers response) "content-type") #";")))

;; revise the algorithm (future)
(defn format-query-params
  "Format query `params`."
  [params]
  (let [params (compact-map params)]
    (if-not (empty? params)
      (->> (transform-keys params
                           (fn [param]
                             (if (string? param)
                               param
                               (-> param name underscore))))
           seq
           flatten
           (apply sorted-map)
           (http/generate-query-string)))))

(defn parse-body
  "Parse `body` and return a map with hypenized keys and their values."
  [body]
  (reduce (fn [m p]
            (let [[k v] (split p #"=")]
              (assoc m (hyphenate (keyword k)) v)))
          {}
          (split body #"&")))

(defn random-base64
  "Returns a Base64 encoded string from a random byte array of the
  specified size."
  [size]
  (http.util/base64-encode (sec/random-bytes size)))

(defn encode-base64-url-safe
  "Return base64 encoded `value` string, safe for url usage."
  [value]
  (-> value
      http.util/base64-encode
      (replace "/" "_")
      (replace "+" "-")))

(defn code-verifier
  "Return random code verifier."
  []
  (-> 30
      sec/random-bytes
      encode-base64-url-safe
      (replace #"[^a-zA-Z0-9]+" "")))

(defn code-challenge
  "Build a challenge code using the `verifier`."
  [verifier]
  (-> verifier
      digest/sha256
      encode-base64-url-safe
      (replace "=" "")))

(defn wrap-content-type
  "Returns a HTTP client that sets the Content-Type header to `request`."
  [client content-type]
  (fn [request]
    (->> (or (:content-type request) content-type)
         (assoc request :content-type)
         (client))))
