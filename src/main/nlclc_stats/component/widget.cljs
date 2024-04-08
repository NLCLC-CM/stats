(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [subset?]]
            [clojure.string :as string]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.serde :refer [edn->base64]]
            [main.nlclc-stats.component.search-bar :as search-bar]))

(defonce tab (r/atom :history))
(defonce selected-key (r/atom nil))
(defonce sort-dates-ascending (r/atom false))

(defn- switch-tabs! [new-tab-name]
  (do
    (reset! tab new-tab-name)
    (reset! selected-key nil)))

(defn- share-this-page! [evt]
  (let [this (.-target evt)
        original-text (.-textContent this)
        share64 (edn->base64 {:tab @tab
                              :query (search-bar/get-query)
                              :selected-key @selected-key
                              :sort-dates-ascending @sort-dates-ascending})
        baseurl js/window.location.href
        url (js/URL. baseurl)]
    (.set (.-searchParams url) "share" share64)
    (js/navigator.clipboard.writeText (str url))
    (set! (.-textContent this) "Copied!")

    (js/setTimeout #(set! (.-textContent this) original-text) 2000)))

(defn- tabs-component []
  (let [props-for (fn [t] {:class ["nav-link" "nav-item" (when (= t @tab) "active")]
                           :on-click #(switch-tabs! t)})]
    [:ul {:class "nav flex-column nav-pills col-sm-2"}
     [:li (props-for :people) "People"]
     [:li (props-for :songs) "Songs"]
     [:li (props-for :roles) "Roles"]
     [:li (props-for :history) "History"]
     
     [:li
      [:button
       {:class "btn btn-outline-light"
        :style {:position "fixed" :bottom "1rem"}
        :on-click #(share-this-page! %)}
       "Share this page!"]]]))

(defn- contains-all-people? [people subset]
  (let [all-people (set (apply concat (vals people)))]
    (subset? subset all-people)))

(defn- contains-all-roles? [people subset]
  (let [all-roles (set (map name (keys people)))]
    (subset? subset all-roles)))

(defn- contains-all-songs? [songs subset]
  (subset? subset (set songs)))

(defn- valid-entry?
  [{:keys [people songs roles starting-date ending-date]
    :or {people (list)
         songs (list)
         roles (list)}}

   {entry-people :entry/people
    entry-songs :entry/songs
    entry-date :entry/date}]
  (and (contains-all-people? entry-people people)
       (contains-all-songs? entry-songs songs)
       (contains-all-roles? entry-people roles)
       
       (or (nil? starting-date) (not (neg? (compare entry-date starting-date))))
       (or (nil? ending-date) (not (pos? (compare entry-date ending-date))))))

(defn- history-content []
  (let [query (search-bar/get-query)
        filtered-history (filter (partial valid-entry? query) data/entries)
        sorted-history (if @sort-dates-ascending filtered-history (reverse filtered-history))
        item->link (fn [item-type item] [:a {:on-click #(search-bar/add-item item-type item)
                                             :href "#"
                                             :title "Add to query"} item])
        role-people->hiccup (fn [[role people]]
                              `[:span
                                ~(item->link "roles" role)
                                ": "
                                ~@(interpose ", " (for [person people]
                                                    (item->link "people" person)))])]
    [:table {:class "table"}
     [:thead
      [:tr
       [:th {:scope "col"
             :style {:cursor "pointer"}
             :on-click #(swap! sort-dates-ascending not)} "Date " (if @sort-dates-ascending "(ASC)" "(DESC)")]
       [:th {:scope "col"} "Lecture title"]
       [:th {:scope "col"} "People involved"]
       [:th {:scope "col"} "Songs"]]]

     `[:tbody
       ~@(for [{:entry/keys [date lecture-name people songs]} sorted-history]
           [:tr
            [:td {:scope "row"} [:time {:dateTime date} date]]
            [:td lecture-name]
            [:td `[:p ~@(interpose [:br] (map role-people->hiccup (into [] people)))]]
            [:td `[:p ~@(interpose [:br] (map (partial item->link "songs") songs))]]])]

     (if (empty? sorted-history)
       [:tfoot
        [:tr [:td {:colSpan 4} "No entries found"]]]
       [:tfoot
        [:tr [:td {:colSpan 4} (count sorted-history) " entries found"]]])]))

(defn- content []
  [:section {:class "col-sm-10"}
   [:div {:class "col"}
    [search-bar/component {:thing-to-search @tab}]

    (case @tab
      :history [history-content]

      [:p {:class "text-danger"} "Unknown tab = " @tab])]])

(defn component [init-state]
  (reset! tab (:tab init-state))
  (reset! selected-key (:selected-key init-state))
  (reset! sort-dates-ascending (:sort-dates-ascending init-state))

  [:section {:class "row"}
   [tabs-component]
   [content]])
