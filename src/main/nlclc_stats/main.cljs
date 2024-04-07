(ns main.nlclc-stats.main
  (:require [reagent.core :as r]
            ["react-dom/client" :refer [createRoot]]
            [cljs.reader]
            [main.nlclc-stats.component.widget :as w]))

(defn b64->state [b64]
  (-> b64
      js/atob
      cljs.reader/read-string))

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
      (b64->state shareB64))))

; https://stackoverflow.com/a/72477660/3988732
; adjustments required for React v18, in particular we use createRoot
(defonce widget-root (createRoot (js/document.getElementById "widget")))

(defn init []
  (.render widget-root (r/as-element [w/component stored-state])))

(defn ^:dev/after-load start []
  (init))
