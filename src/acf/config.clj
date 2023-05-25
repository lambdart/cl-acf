(ns acf.config
  (:require [clojure.string :refer [join]]))

(def twitter-scopes
  ["tweet.read"
   "tweet.write"
   "tweet.moderate.write"
   "users.read"
   "follows.read"
   "follows.write"
   "offline.access"
   "space.read"
   "mute.read"
   "mute.write"
   "like.read"
   "like.write"
   "list.read"
   "list.write"
   "block.read"
   "block.write"
   "bookmark.read"
   "bookmark.write"])

(def twitter
  {:access-token-url "https://api.twitter.com/2/oauth2/token"
   :authorization-url "https://twitter.com/i/oauth2/authorize"
   :request-token-url "https://api.twitter.com/2/oauth2/token"
   :default-options {:state "random"
                     ;; :code-challenge-method "S256"
                     :code-challenge-method "plain"
                     :response-type "code"
                     :scope (join " " twitter-scopes)}})

(def google-scopes
  ["https://www.googleapis.com/auth/userinfo.email"
   "https://www.googleapis.com/auth/userinfo.profile"])

(def google
  {:access-token-url "https://accounts.google.com/o/oauth2/token"
   :authorization-url "https://accounts.google.com/o/oauth2/auth"
   :request-token-url "https://accounts.google.com/o/oauth2/token"
   :default-options {:access-type "offline"
                     :response-type "code"
                     :scope (join " " google-scopes)}})
