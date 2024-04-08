(ns main.nlclc-stats.serde
  (:require [cljs.reader]))

(defn edn->base64 [edn]
  (-> edn
      prn-str
      js/encodeURIComponent
      js/unescape
      js/btoa))

(defn base64->edn [b64]
  (-> b64
      js/atob
      js/escape
      js/decodeURIComponent
      cljs.reader/read-string))
