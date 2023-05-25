(ns acf.core
  (:require [acf.v2 :as v2]))

(defn oauth-authorization-url
  "Return oauth authorization url."
  [{:keys [authorization-url default-options]}
   {:keys [client-id code-challenge scope redirect-uri]}]
  (let [params {:url authorization-url
                :client-id client-id
                :redirect-uri redirect-uri}
        options (merge default-options
                       {:scope scope}
                       ;; oauth psck
                       (if (nil? code-challenge)
                         {}
                         {:code-challenge code-challenge}))]
    ;; return the parsed oauth authorization url
    (v2/oauth-authorization-url params options)))

(defn oauth-access-token
  "Return the access token."
  [{:keys [access-token-url]} options]
  (-> options
      (select-keys [:client-id
                    :client-secret
                    :code
                    :code-verifier
                    :redirect-uri
                    :grant-type])
      (merge {:url access-token-url})
      (v2/oauth-access-token)))

(defn oauth-refresh-access-token
  "Return the access token."
  [{:keys [request-token-url]} options]
  (-> (dissoc options
              :scope
              :grant-type
              :code-challenge
              :code-verifirer)
      (merge {:url request-token-url})
      (v2/oauth-refresh-access-token)))

(defn oauth-client
  "Return an oauth client with `access-token` wrapped to it."
  [{:keys [access-token token-type]}]
  (v2/oauth-client access-token
                   (or token-type nil)))
