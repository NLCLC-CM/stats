(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [subset?]]
            [clojure.string :as string]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.serde :refer [url->state state->url]]
            [main.nlclc-stats.component.search-bar :as search-bar]))

(defonce stored-state
  (let [url-state (url->state js/window.location)]
    (if (nil? (:tab url-state))
      (r/atom {:tab :history            ; keyword
               :selected-key nil        ; str
               :sort-dates-asc? false   ; bool
               :query {:people #{}
                       :songs #{}
                       :roles #{}
                       :starting-date nil
                       :ending-date nil}})
      (r/atom url-state))))

(defn- switch-tabs! [new-tab-name]
  (when (not= (:tab @stored-state) new-tab-name)
    (swap! stored-state assoc :tab new-tab-name)
    (swap! stored-state assoc :selected-key nil)))

(defn- share-this-page! [evt]
  (let [this (.-target evt)
        original-text (.-textContent this)
        baseurl (str js/window.location.origin js/window.location.path)
        url (state->url baseurl @stored-state)]
    (js/navigator.clipboard.writeText url)
    (set! (.-textContent this) "Copied!")

    (js/setTimeout #(set! (.-textContent this) original-text) 2000)))

(defn- tabs-component []
  (let [props-for (fn [t] {:class ["nav-link" "nav-item" (when (= t (:tab @stored-state)) "active")]
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
  (let [query (:query @stored-state)
        filtered-history (filter (partial valid-entry? query) data/entries)
        sorted-history (if (:sort-dates-asc? @stored-state) filtered-history (reverse filtered-history))
        item->link (fn [item-type item] [:a {:on-click #(search-bar/add-item item-type item stored-state)
                                             :key (str item-type \- item)
                                             :style {:display "block"
                                                     :marginBottom "0.2rem"}
                                             :href "#"
                                             :title "Add to query"} item])
        role-people->hiccup (fn [[role people]]
                              [:div
                               {:style {:display "grid"
                                        :gridTemplateColumns "1fr 2fr"}
                                :key role}
                               [:div role]
                               [:div (map (partial item->link "people") people)]])]
    [:table {:class "table"}
     [:thead
      [:tr
       [:th {:scope "col"
             :style {:cursor "pointer"}
             :on-click #(swap! stored-state update :sort-dates-asc? not)}
        "Date "
        (if (:sort-dates-asc? @stored-state)
          "(ASC)"
          "(DESC)")]
       [:th {:scope "col"} "Lecture title"]
       [:th {:scope "col"} "People involved"]
       [:th {:scope "col"} "Songs"]]]

     [:tbody
      (for [{:entry/keys [date lecture-name people songs]} sorted-history]
        ^{:key date}

        [:tr
         [:td {:scope "row"} [:time {:dateTime date} date]]
         [:td lecture-name]
         [:td (map role-people->hiccup (into [] people))]
         [:td [:p (map (partial item->link "songs") songs)]]])]

     (if (empty? sorted-history)
       [:tfoot
        [:tr [:td {:colSpan 4} "No entries found"]]]
       [:tfoot
        [:tr [:td {:colSpan 4} (count sorted-history) " entries found"]]])]))

(defn- people-content []
  (let [query (:query @stored-state)
        filtered-history (filter (partial valid-entry? query) data/entries)
        people (sort-by first (into [] (data/people-data filtered-history '(:av :usher))))]
    [:div
     (for [[person-name {:keys [frequency starting-date ending-date]}] people]
       ^{:key person-name}

       [:div {:class "card" :style {:width "18rem"}}
        [:img {:src "" :class "card-img-top"}]
        [:div {:class "card-body"}
         [:h5 {:class "card-title"} person-name]
         [:small {:class "text-body-secondary"}
          [:time {:dateTime starting-date} starting-date]
          " to "
          [:time {:dateTime ending-date} ending-date]]]])]))

(defn- content []
  [:section {:class "col-sm-10"}
   [:div {:class "col"}
    [search-bar/component stored-state]

    (case (:tab @stored-state)
      :people [people-content]
      :songs [:p "Songs!"]
      :roles [:p "Roles!"]
      :history [history-content]

      [:p {:class "text-danger"} "Unknown tab = " (:tab @stored-state)])]])

(defn component []
  [:section {:class "row"}
   [tabs-component]
   [content]])
