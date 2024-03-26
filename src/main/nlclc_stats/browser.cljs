(ns main.nlclc-stats.browser)

(defn ^:dev/after-load start []
  :after-code-reload)

(defn init []
  (js/console.log "hello world"))

(defn ^:dev/before-load stop []
  :before-code-is-reloaded)
