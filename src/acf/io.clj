(ns acf.io
  (:require [clj-http.client :refer [wrap-request]]
            [clj-http.core :as http]
            [acf.serialize :refer :all]
            [acf.helpers :refer [hyphenate-keys]]))

(defn wrap-meta-response
  "Wrap meta response."
  [handler]
  (fn [request]
    (let [{:keys [body] :as response} (handler request)]
      (if (instance? clojure.lang.IMeta body)
        (with-meta body (dissoc response :body))
        body))))

(defn wrap-input-coercion
  "Wrap input coercion."
  [handler]
  (fn [request]
    (handler (serialize request))))

(defn wrap-output-coercion
  "Wrap output coercion."
  [handler]
  (fn [request]
    (if (= :stream (:as request))
      (handler request)
      (-> (handler request)
          (deserialize)))))

(defn wrap-output-hyphenate
  "Returns a HTTP client that recursively replaces all underscores in
  the keys of the response map to dashes."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (:skip-hyphenate request)
        response (hyphenate-keys response)))))

(def request
  (-> #'http/request
      (wrap-request)
      (wrap-input-coercion)
      (wrap-output-coercion)
      (wrap-output-hyphenate)
      (wrap-meta-response)))
