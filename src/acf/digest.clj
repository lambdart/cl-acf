(ns acf.digest
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)
           (java.security MessageDigest)))

(defn hmac
  ([^String algorithm ^String msg ^String key]
   (hmac algorithm msg key "UTF8"))
  ([^String algorithm ^String msg ^String key ^String encoding]
   (let [key (SecretKeySpec. (.getBytes key "UTF8") algorithm)
         mac (doto (Mac/getInstance algorithm)
               (.init key))]
     (.doFinal mac (.getBytes msg encoding)))))

(defn sha256
  "Compute `input` sha256 digest."
  ([^String input] (sha256 input "SHA-256"))
  ([^String input ^String algorithm]
   (let [hash (MessageDigest/getInstance algorithm)]
     (do
       (. hash update (.getBytes input))
       (.digest hash)))))
