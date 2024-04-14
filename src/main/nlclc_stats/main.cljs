(ns main.nlclc-stats.main
  (:require [reagent.core :as r]
            ["react-dom/client" :refer [createRoot]]
            [main.nlclc-stats.component.search-bar :as search-bar]
            [main.nlclc-stats.component.widget :as w]))

; https://stackoverflow.com/a/72477660/3988732
; adjustments required for React v18, in particular we use createRoot
(defonce widget-root (createRoot (js/document.getElementById "widget")))

(defn init []
  (.render widget-root (r/as-element [w/component])))

(defn ^:dev/after-load start []
  (init))
