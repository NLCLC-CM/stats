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

(comment
  (def state {:tab :history
              :selected-key nil
              :sort {:dir :asc
                     :k nil}
              :query {:people #{"Cheuk"}
                      :songs #{}
                      :roles #{}
                      :starting-date nil
                      :ending-date nil}})

  (def u (js/URL. js/window.location))

  (for [person (get-in state [:query :people])]
    (.searchParams.append u "a" person))

  (def converted (state->url state "http://localhost/"))
  (url->state converted))

(defn url->state
  "Given some string url from window.location, get the state."
  [url-string]
  (let [url (js/URL. url-string)
        state (atom {})]
    (swap! state assoc :tab (keyword (.searchParams.get url "tab")))
    (swap! state assoc-in [:sort :k] (keyword (.searchParams.get url "sort-k")))
    (swap! state assoc-in [:sort :dir] (keyword (.searchParams.get url "sort-dir")))
    (swap! state assoc :selected-key (.searchParams.get url "selected-key"))
    (swap! state assoc-in [:query :people] (into #{} (.searchParams.getAll url "query-people")))
    (swap! state assoc-in [:query :songs] (into #{} (.searchParams.getAll url "query-songs")))
    (swap! state assoc-in [:query :roles] (into #{} (.searchParams.getAll url "query-roles")))
    (swap! state assoc-in [:query :starting-date] (.searchParams.get url "query-starting-date"))
    (swap! state assoc-in [:query :ending-date] (.searchParams.get url "query-ending-date"))

    @state))

(defn state->url
  "Base url is just the origin and the path name. it should be a string."
  [base-url state]
  (let [url (js/URL. base-url)]
    (.searchParams.set url "tab" (name (:tab state)))

    (when (not (nil? (:selected-key state)))
      (.searchParams.set url "selected-key" (:selected-key state)))

    (.searchParams.set url "sort-dir" (-> state :sort :dir name))

    (when (not (nil? (-> state :sort :k)))
      (.searchParams.set url "sort-k" (-> state :sort :k name)))

    (doseq [person (get-in state [:query :people])]
      (.searchParams.append url "query-people" person))

    (doseq [song (get-in state [:query :songs])]
      (.searchParams.append url "query-songs" song))

    (doseq [role (get-in state [:query :roles])]
      (.searchParams.append url "query-roles" role))

    (when (not (nil? (get-in state [:query :starting-date])))
      (.searchParams.set url "query-starting-date" (get-in state [:query :starting-date])))

    (when (not (nil? (get-in state [:query :ending-date])))
      (.searchParams.set url "query-ending-date" (get-in state [:query :ending-date])))

    (str url)))
