(ns acf.serialize
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? replace]]
            [cheshire.core :as json]
            [acf.const :refer [application-json
                                 application-clojure]]
            [acf.util :refer [parse-body]]))

(defn content-type
  "Returns the value of the Content-Type header of `request`."
  [request]
  (let [content-type (get (:headers request) "content-type")]
    (if-not (blank? content-type)
      (keyword (replace content-type #";.*" "")))))

(defn deserialize-body
  "Update the :body of `response` by applying `update-fn` to it, if
  it's a string."
  [{:keys [body] :as response} update-fn]
  (if (string? body)
    (update-in response [:body] update-fn)
    response))

(defmulti deserialize
  "Deserialize the body of `response` according to the Content-Type header."
  (fn [response]
    (content-type response)))

(defmethod deserialize :default
  [response] response)

(defmethod deserialize :application/clojure
  [response]
  (binding [*read-eval* false]
    (deserialize-body response read-string)))

(defmethod deserialize :application/json
  [response]
  (deserialize-body response #(json/decode % true)))

(defmethod deserialize :application/x-www-form-urlencoded
  [response]
  (deserialize-body response parse-body))

(defmethod deserialize :text/html
  [response]
  (deserialize-body response parse-body))

(defmethod deserialize :text/javascript
  [response]
  (deserialize-body response #(json/decode % true)))

(defmethod deserialize :text/plain
  [response]
  (deserialize-body response parse-body))

(defn serialize-body
  "Update the :body of `response` by applying `update-fn` to it
  and associate the headers content-type with `content-type`."
  [request content-type update-fn]
  (if (:body request)
    (-> (update-in request [:body] update-fn)
        (assoc-in [:headers "content-type"] content-type))
    request))

(defmulti serialize
  "Serialize the body of `response` according to the Content-Type header."
  (fn [request]
    (content-type request)))

(defmethod serialize :default
  [request] request)

(defmethod serialize :application/clojure
  [request]
  (serialize-body request application-clojure prn-str))

(defmethod serialize :application/json
  [request]
  (serialize-body request application-json json/encode))
