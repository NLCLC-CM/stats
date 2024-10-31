(ns nlclc-stats.build
  (:require [hiccup2.core :as h]
            [hiccup.page :as p]))

(defn- template [& content]
  (p/html5
    [:head
     [:title "NLCLC Stats"]
     [:link {:rel "stylesheet"
             :src "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
             :integrity "sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
             :crossorigin "anonymous"}]
     [:script {:type "text/javascript"
               :src "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
               :integrity "sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
               :crossorigin "anonymous"}]]
    [:body.bg-dark {:data-bs-theme "dark"}
     content]))

(defn- index []
  (template
    "hello"
    "world"))

(defn -main
  ([] (println "missing destination parameter"))
  ([path] (println "path" path "specified!")))
