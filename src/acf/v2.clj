(ns acf.v2
  (:require [acf.const :refer [x-www-form-urlencoded]]
            [acf.helpers :refer [transform-keys
                                   underscore]]
            [acf.util :refer [format-query-params
                                wrap-content-type]]
            [acf.io :refer [request]]))

(defn- update-access-token
  "Update request access token."
  [request access-token]
  (assoc-in request
            [:query-params "access_token"]
            access-token))

(defn- update-authorization-header
  "Update authorization `request` map header with the `access-token`."
  [request access-token]
  (assoc-in request
            [:headers "authorization"]
            (str "Bearer " access-token)))

(defn oauth-authorization-url
  "Returns the OAuth authorization url."
  [{:keys [url
           client-id
           redirect-uri] :as params} options]
  (->> (dissoc params :url)
       (merge options)
       (format-query-params)
       (str url "?")))

(defn- oauth-http-request
  "Make the request."
  [{:keys [url] :as params}]
  (try
    (request {:method :post
              :url url
              :form-params (transform-keys (dissoc params :url)
                                           (comp underscore name))})
    ;; add logs (future)
    ;; handle errors or throw+ (future)
    (catch Exception e e)))

(defn oauth-access-token
  "Obtain the OAuth access token."
  [params]
  (->> {:grant-type "authorization_code"}
       (merge params)
       (oauth-http-request)))

(defn oauth-refresh-access-token
  "Make refresh token request."
  [params]
  (->> {:grant-type "refresh_token"}
       (merge params)
       (oauth-http-request)))

(defn wrap-oauth-access-token
  "Returns a HTTP client that adds the OAuth `access-token` to `request`."
  [client access-token & [token-type]]
  (fn [request]
    (client
     (apply (if (= token-type "bearer")
              update-authorization-header
              update-access-token)
            [request access-token]))))

(defn oauth-client
  "Returns a HTTP client for version 2 of the OAuth protocol."
  [access-token token-type]
  (-> request
      (wrap-content-type x-www-form-urlencoded)
      (wrap-oauth-access-token access-token
                               token-type)))
