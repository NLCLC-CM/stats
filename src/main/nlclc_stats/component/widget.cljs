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

(defn- tab-component [tab]
  [:li {:class "nav-item"
        :on-click #(switch-tabs! tab)}
   [:a {:href "#"
        :class ["nav-link" (when (= tab (:tab @stored-state)) "active")]}
    (string/capitalize (name tab))]])

(defn- share-btn []
  [:button
   {:class ["btn" "btn-outline-light"]
    :style {:position "fixed"
            :bottom "1rem"}
    :on-click share-this-page!}
   "Share this page!"])

(defn- tabs-component []
  [:ul {:class "nav flex-column nav-pills col-sm-2"}
   [tab-component :people]
   [tab-component :songs]
   [tab-component :roles]
   [tab-component :history]

   [:li
    [share-btn]]])

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
        item->link (fn [item-type item]
                     [:div {:class ["btn-group"]
                            :role "group"
                            :key (str item-type \- item)}
                      [:button {:class ["btn" "btn-outline-light"]
                                :style {:width "100%"}
                                :on-click #(do (swap! stored-state assoc :tab item-type)
                                               (swap! stored-state assoc :selected-key item))
                                :title "View"}
                       item]
                      [:button {:class ["btn" "btn-outline-light"]
                                :on-click #(search-bar/add-item (name item-type) item stored-state)
                                :title "Add to query"}
                       \+]])
        role-people->hiccup (fn [[role people]]
                              [:div
                               {:style {:display "grid"
                                        :gridTemplateColumns "1fr 2fr"}
                                :key role}
                               [:div role]
                               [:div {:style {:display "flex" :flexDirection "column"}} (map (partial item->link :people) people)]])]
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
         [:td [:div {:style {:display "flex" :flexDirection "column"}} (map (partial item->link :songs) songs)]]])]

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
         [:h5 {:class "card-title" :on-click #(swap! stored-state assoc :selected-key person-name)} person-name]
         [:small {:class "text-body-secondary"}
          "Appeared " frequency " times."]]])]))

(defn- popular-songs [person]
  (let [filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        songs (reverse (take-last 5 (sort-by second (into [] (data/songs-frequencies filtered-history)))))]
    [:section {:class "col"}
     [:table {:class "table"}
      [:thead
       [:tr
        [:th {:scope "col"} "Song name"]
        [:th {:scope "col"} "Occurances"]]]

      [:tbody
       (for [[song-name freq] songs]
         ^{:key song-name}

         [:tr
          [:td song-name]
          [:td freq]])]]]))

(defn- popular-partners [person]
  (let [filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        partners (reverse (take-last 5 (sort-by second (into [] (data/people-frequencies filtered-history #{:av :usher})))))]
    [:section {:class "col"}
     [:table {:class "table"}
      [:thead
       [:tr
        [:th {:scope "col"} "Partner name"]
        [:th {:scope "col"} "Occurances"]]]

      [:tbody
       (for [[partner-name freq] partners
             :when (not= partner-name person)]
         ^{:key partner-name}

         [:tr
          [:td {:on-click #(swap! stored-state assoc :selected-key partner-name)} partner-name]
          [:td freq]])]]]))

(defn- popular-roles [person]
  (let [filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        roles (reverse (take-last 5 (sort-by second (into [] (data/roles-frequencies filtered-history person)))))]
    [:section {:class "col"}
     [:table {:class "table"}
      [:thead
       [:tr
        [:th {:scope "col"} "Role name"]
        [:th {:scope "col"} "Occurances"]]]

      [:tbody
       (for [[role-name freq] roles]
         ^{:key role-name}

         [:tr
          [:td role-name]
          [:td freq]])]]]))

(defn- person-content [person]
    [:div
     [:a {:href "#" :on-click #(swap! stored-state assoc :selected-key nil)} "back"]
     [:br]
     [:br]
     [:h4 person]

     [:div {:class "row"}
      [popular-songs person]
      [popular-partners person]
      [popular-roles person]]])

(defn- content []
  [:section {:class "col-sm-10"}
   [:div {:class "col"}
    (when (nil? (:selected-key @stored-state))
      [search-bar/component stored-state])

    (case (:tab @stored-state)
      :people (if (nil? (:selected-key @stored-state))
                [people-content]
                [person-content (:selected-key @stored-state)])
      :songs [:p "Songs!"]
      :roles [:p "Roles!"]
      :history [history-content]

      [:p {:class "text-danger"} "Unknown tab = " (:tab @stored-state)])]])

(defn component []
  [:section {:class "row"}
   [tabs-component]
   [content]])
