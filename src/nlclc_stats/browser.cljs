(ns nlclc-stats.browser
  (:require [nlclc-stats.data.year2023]))

(defn ^:dev/after-load start []
  :after-code-reload)

(defn init []
  (js/console.log nlclc-stats.data.year2023/q1))

(defn ^:dev/before-load stop []
  :before-code-is-reloaded)
