(ns main.nlclc-stats.main
  (:require [reagent.core :as r]
            ["react-dom/client" :refer [createRoot]]
            [main.nlclc-stats.serde :refer [base64->edn]]
            [main.nlclc-stats.component.search-bar :as search-bar]
            [main.nlclc-stats.component.widget :as w]))

(defonce stored-state
  (let [shareB64 (-> js/window
                     .-location
                     .-href
                     js/URL.
                     .-searchParams
                     (.get "share"))]
    (if (nil? shareB64)
      {:query {}
       :tab :history
       :selected-key nil
       :sort-dates-ascending false}
      (base64->edn shareB64))))

; https://stackoverflow.com/a/72477660/3988732
; adjustments required for React v18, in particular we use createRoot
(defonce widget-root (createRoot (js/document.getElementById "widget")))

(defn init []
  (search-bar/set-query! (:query stored-state {}))
  (.render widget-root (r/as-element [w/component stored-state])))

(defn ^:dev/after-load start []
  (init))
