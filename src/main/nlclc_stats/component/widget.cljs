(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [subset?]]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.component.search-bar :as search-bar]))

(defonce tab (r/atom :history))
(defonce query (r/atom {}))
(defonce selected-key (r/atom nil))
(defonce sort-dates-ascending (r/atom false))

(defn- switch-tabs! [new-tab-name]
  (do
    (reset! tab new-tab-name)
    (reset! selected-key nil)))

(defn- tabs-component []
  (let [props-for (fn [t] {:class ["nav-link" "nav-item" (when (= t @tab) "active")]
                           :on-click #(switch-tabs! t)})]
    [:ul {:class "nav flex-column nav-pills col-sm-2"}
     [:li (props-for :people) "People"]
     [:li (props-for :songs) "Songs"]
     [:li (props-for :roles) "Roles"]
     [:li (props-for :history) "History"]]))

(defn- contains-all-people? [people subset]
  (let [all-people (set (apply concat (vals people)))
        subset (set subset)]
    (subset? subset all-people)))

(defn- contains-all-roles? [people subset]
  (let [all-roles (set (map str (keys people)))
        subset (set subset)]
    (subset? subset all-roles)))

(defn- contains-all-songs? [songs subset]
  (subset? (set subset) (set songs)))

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
  (let [filtered-history (filter (partial valid-entry? @query) data/entries)
        sorted-history (if @sort-dates-ascending filtered-history (reverse filtered-history))]
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
       ~@(for [{:entry/keys [date lecture-name]} sorted-history]
           [:tr
            [:td {:scope "row"} [:time {:datetime date} date]]
            [:td lecture-name]
            [:td "placeholder"]
            [:td "placeholder"]])]

     (if (empty? sorted-history)
       [:tfoot
        [:tr {:colspan 4} "No entries found"]]
       [:tfoot
        [:tr {:colspan 4} (count sorted-history) " entries found"]])]))

(defn- content []
  [:section {:class "col-sm-10"}
   [:div {:class "col"}
    [search-bar/component {:thing-to-search @tab
                           :on-change #(reset! query %)}]

    (case @tab
      :history [history-content]

      [:p {:class "text-danger"} "Unknown tab = " @tab])]])

(defn component []
  [:section {:class "row"}
   [tabs-component]
   [content]])
