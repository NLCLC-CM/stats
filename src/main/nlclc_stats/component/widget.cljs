(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [subset? difference]]
            [clojure.string :as string]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.serde :refer [url->state state->url]]
            [main.nlclc-stats.component.search-bar :as search-bar]))

(defonce clicks-on-my-name (r/atom (or (js/window.localStorage.getItem "clicks") 0)))
(defonce flipped? (r/atom false))
(defonce role-tab (r/atom 0))

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
        baseurl (str js/window.location.origin js/window.location.pathname)
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
   {:class ["btn" "btn-outline-light" "position-fixed" "bottom-0"]
    :style {:margin "1rem"}
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

(defn- item->link
  [item-type item]
  [:div {:class ["btn-group"]
         :role "group"
         :style {:marginBottom "0.2rem"}
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

(defn- role-people->hiccup
  [[role people]]
  [:div
   {:style {:display "grid"
            :gridTemplateColumns "1fr 1.5fr"}
    :key role}
   [item->link :roles (name role)]

   [:div {:style {:display "flex" :flexDirection "column"}}
    (map (partial item->link :people) people)]])

(defn- history-content []
  (let [query (:query @stored-state)
        filtered-history (filter (partial valid-entry? query) data/entries)
        sorted-history (if (:sort-dates-asc? @stored-state) filtered-history (reverse filtered-history))]
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
     {:class ["d-flex" "flex-row" "flex-wrap"]}
     (for [[person-name {:keys [frequency starting-date ending-date]}] people]
       ^{:key person-name}

       [:div {:class "card"
              :style {:width "15rem"
                      :cursor "pointer"}
              :on-click #(swap! stored-state assoc :selected-key person-name)}
        [:img {:src "" :class "card-img-top"}]
        [:div {:class "card-body"}
         [:h5 {:class "card-title"} person-name]
         [:small {:class "text-body-secondary"}
          "Appeared " frequency " times."]]])]))

(defn- songs-content []
  (let [query (:query @stored-state)
        filtered-history (filter (partial valid-entry? query) data/entries)
        songs (sort-by first (into [] (data/songs-frequencies filtered-history)))]
    [:div
     {:class ["d-flex" "flex-row" "flex-wrap"]}
     (for [[song-name hits] songs]
       ^{:key song-name}

       [:div {:class "card"
              :style {:width "15rem"
                      :cursor "pointer"}
              :on-click #(swap! stored-state assoc :selected-key song-name)}
        [:div {:class "card-body"}
         [:h5 {:class "card-title"} song-name]
         [:small {:class "text-body-secondary"}
          "Appeared " hits " times."]]])]))

(defn- song-history [dates]
  [:section {:class "col"}
   [:h5 "Appearances"]
   [:div {:class "list-group"}
    (for [date dates]
      ^{:key date}
      [:button {:class ["list-group-item" "list-group-item-action"]
                :on-click #(do (swap! stored-state assoc :tab :history)
                               (swap! stored-state assoc :query {:songs #{(:selected-key @stored-state)}})
                               (swap! stored-state assoc :selected-key nil))}
       date])]])

(defn- popular-songs [person]
  (let [filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        songs (reverse (sort-by second (into [] (data/songs-frequencies filtered-history))))]
    [:section {:class "col"}
     [:h5 "Favourite songs"]
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

(defn- song-faves [favourites]
  [:section {:class "col"}
   [:h5 "Favourite pairings"]
   [:small "Just like having good meat-wine pairings, certain songs pair well with others. At least, that's how we choose to interpret the data."]
   [:table {:class "table"}
    [:thead
     [:tr
      [:th {:scope "col"} "Song name"]
      [:th {:scope "col"} "Occurances"]]]

    [:tbody
     (for [[song-name freq] favourites]
       ^{:key song-name}

       [:tr
        [:td song-name]
        [:td freq]])]]])

(defn- blacklisted-roles-by-enum
  [enum]
  (case enum
    0 #{:av :usher}
    1 (difference data/roles #{:av})
    2 (difference data/roles #{:usher})))

(defn- popular-partners [person]
  (let [blacklist (blacklisted-roles-by-enum @role-tab)
        filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        partners (reverse (sort-by second (into [] (data/people-frequencies filtered-history blacklist))))]

    [:section {:class "col"}

     (if @flipped?
       [:div {:class "row"}
        (case @role-tab
          0 [:div {:class "col"}
             [:h5 {:class "col-10"}
              "Favourite people (to work with)"]
             [:small "This implies that there are 'least favourite people (to work with)', and we don't go there."]]
          1 [:h5 {:class "col-10"} "AV team!"]
          2 [:h5 {:class "col-10"} "Ushers"])
        [:button {:class ["col-2" "btn" "btn-outline-secondary"]
                  :on-click #(swap! role-tab (comp (fn [x] (mod x 3)) inc))} \>]]
       [:h5 {:title "This implies that there are 'least favourite people (to work with)', and we don't go there."} "Favourite people (to work with)"])

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
        roles (reverse (sort-by second (into [] (data/roles-frequencies filtered-history person))))]
    [:section {:class "col"}
     [:h5 "Favourite roles"]
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
   [:div {:class "row"}
    [:h4 {:class "col-sm-8"
          :on-click #(do (swap! clicks-on-my-name inc)
                         (js/window.localStorage.setItem "clicks" @clicks-on-my-name))}
     person]

    (when (>= @clicks-on-my-name 5)
      [:div {:on-click #(swap! flipped? not)
             :class ["form-check" "form-switch" "col-sm-4" (when @flipped? "fade-out")]}
       [:input {:class "form-check-input"
                :type "checkbox"
                :on-change #(reset! flipped? (.-checked (.-target %)))
                :checked @flipped?
                :id "flipped-the-switch"}]
       [:label {:for "flipped-the-switch"
                :class "form-check-label"}
        (if @flipped?
          "Advanced mode enabled"
          "Advanced mode disabled")]])]

   [:div {:class "row"}
    [popular-songs person]
    [popular-partners person]
    [popular-roles person]]])

(defn- song-content [song-name]
  [:div
   [:a {:href "#" :on-click #(swap! stored-state assoc :selected-key nil)} "back"]
   [:br]
   [:br]
   [:div {:class "row"}
    [:h4 {:class "col-sm-8"}
     song-name]]

   (let [{:keys [history favourites]} (data/song-data song-name data/entries)]
     [:div {:class "row"}
      [song-history history]
      [song-faves favourites]])])

(defn- content []
  [:section {:class "col-sm-10"}
   [:div {:class "col"}
    (when (nil? (:selected-key @stored-state))
      [search-bar/component stored-state])

    (case (:tab @stored-state)
      :people (if (nil? (:selected-key @stored-state))
                [people-content]
                [person-content (:selected-key @stored-state)])
      :songs (if (nil? (:selected-key @stored-state))
               [songs-content]
               [song-content (:selected-key @stored-state)])
      :roles [:p "Roles!"]
      :history [history-content]

      [:p {:class "text-danger"} "Unknown tab = " (:tab @stored-state)])]])

(defn component []
  [:section {:class "row"}
   [tabs-component]
   [content]])
