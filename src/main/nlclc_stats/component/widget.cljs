(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [subset? difference]]
            [clojure.string :as string]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.serde :refer [url->state state->url]]
            [main.nlclc-stats.component.search-bar :as search-bar]))

(defonce simple-query (r/atom ""))

(defn- dir-not [dir]
  (case dir
    :asc :desc
    :desc :asc
    :asc))

(defn- share-this-page! [stored-state evt]
  (let [this (.-target evt)
        original-text (.-textContent this)
        baseurl (str js/window.location.origin js/window.location.pathname)
        url (state->url baseurl @stored-state)]
    (js/navigator.clipboard.writeText url)
    (set! (.-textContent this) "Copied!")

    (js/setTimeout #(set! (.-textContent this) original-text) 2000)))

(defn- switch-tabs! [stored-state tab]
  (swap! stored-state assoc :sort {:dir :asc :k nil})
  (swap! stored-state assoc :tab tab)
  (swap! stored-state assoc :selected-key nil))

(defn- tab-component [stored-state tab]
  [:li {:class "nav-item"
        :on-click #(switch-tabs! stored-state tab)}
   [:a {:href "#"
        :class ["nav-link" (when (= tab (:tab @stored-state)) "active")]}
    (string/capitalize (name tab))]])

(defn- share-btn [stored-state]
  [:button
   {:class ["btn" "btn-outline-light" "position-fixed" "bottom-0"]
    :style {:margin "1rem"}
    :on-click (partial share-this-page! stored-state)}
   "Share this page!"])

(defn- tabs-component [stored-state]
  [:ul {:class "nav flex-column nav-pills col-sm-2"}
   [tab-component stored-state :people]
   [tab-component stored-state :songs]
   [tab-component stored-state :roles]
   [tab-component stored-state :history]

   [:li
    [share-btn stored-state]]])

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
  [stored-state item-type item]
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
  [stored-state [role people]]
  [:div
   {:style {:display "grid"
            :gridTemplateColumns "1fr 1.5fr"}
    :key role}
   [item->link stored-state :roles (name role)]

   [:div {:style {:display "flex" :flexDirection "column"}}
    (map (partial item->link stored-state :people) people)]])

(defn- sort-history
  [history {:keys [dir k]}]
  "Sort all history based on some sort of key `k` and some direction `dir`"
  (let [sort-direction (if (= dir :asc) identity reverse)]
    (case k
      :date (sort-direction history)
      (sort-direction history))))

(defn- on-click-header
  [stored-state header]
  (let [not-dir #(if (= % :asc) :desc :asc)
        swap-dir #(swap! stored-state update-in [:sort :dir] not-dir)
        {:keys [dir k]} (:sort @stored-state)]
    (if (= k header)
      (swap-dir)
      (swap! stored-state assoc-in [:sort :k] header))))

(defn- sort-direction
  [stored-state]
  (case (-> @stored-state :sort :dir)
    :asc "△"
    :desc "▽"
    ""))

(defn- history-content [stored-state]
  (let [query (:query @stored-state)
        filtered-history (filter (partial valid-entry? query) data/entries)
        sorted-history (sort-history filtered-history (:sort @stored-state))]
    [:table {:class "table"}
     [:thead
      [:tr
       [:th {:scope "col"
             :style {:cursor "pointer"}
             :on-click #(do (swap! stored-state update-in [:sort :dir] dir-not)
                            (swap! stored-state assoc-in [:sort :k] :date))}
        "Date "
        [sort-direction stored-state]]
       [:th {:scope "col"} "Lecture title"]
       [:th {:scope "col"} "People involved"]
       [:th {:scope "col"} "Songs"]]]

     [:tbody
      (for [{:entry/keys [date lecture-name people songs]} sorted-history]
        ^{:key date}

        [:tr
         [:td {:scope "row"} [:time {:dateTime date} date]]
         [:td lecture-name]
         [:td (map (partial role-people->hiccup stored-state) (into [] people))]
         [:td [:div {:style {:display "flex" :flexDirection "column"}} (map (partial item->link stored-state :songs) songs)]]])]

     (if (empty? sorted-history)
       [:tfoot
        [:tr [:td {:colSpan 4} "No entries found"]]]
       [:tfoot
        [:tr [:td {:colSpan 4} (count sorted-history) " entries found"]]])]))

(defn- table-headers [stored-state ks]
  (let [th (fn [k]
             [:th
              {:on-click #(do (swap! stored-state assoc-in [:sort :k] k)
                              (swap! stored-state update-in [:sort :dir] dir-not))}
              (if (= (-> @stored-state :sort :k) k)
                (str (name k)
                     " "
                     (sort-direction stored-state))
                (name k))])]
    [:thead 
     [:tr
      (for [k ks]
        ^{:key k}

        [th k])]]))

(defn- people-content [stored-state]
  [:table {:class "table"}
   [table-headers stored-state '(:name :hits)]

   [:tbody
    (let [people ((if (= :asc (-> @stored-state :sort :dir)) identity reverse)
                  (sort-by
                    (if (= (-> @stored-state :sort :k) :name) first second)
                    (into [] (data/people-data data/entries '(:av :usher)))))
          filtered-people (filter (comp #(string/includes? % @simple-query) first) people)]
      (doall (for [[person-name frequency] filtered-people]
               ^{:key person-name}

               [:tr {:on-click #(swap! stored-state assoc :selected-key person-name)}
                [:td person-name]
                [:td frequency]])))]])

(defn- songs-content [stored-state]
  [:table {:class "table"}
   [table-headers stored-state '(:name :hits)]

   [:tbody
    (let [songs ((if (= :asc (-> @stored-state :sort :dir)) identity reverse)
                 (sort-by
                   (if (= (-> @stored-state :sort :k) :name) first second)
                   (into [] (data/songs-frequencies data/entries))))
          filtered-songs (filter (comp #(string/includes? % @simple-query) first) songs)]
      (doall (for [[song-name frequency] filtered-songs]
               ^{:key song-name}

               [:tr {:on-click #(swap! stored-state assoc :selected-key song-name)}
                [:td song-name]
                [:td frequency]])))]])

(defn- song-history [stored-state dates]
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

(defn- popular-partners [stored-state person]
  (let [blacklist (blacklisted-roles-by-enum 0)
        filtered-history (filter (partial valid-entry? {:people #{person}}) data/entries)
        partners (reverse (sort-by second (into [] (data/people-frequencies filtered-history blacklist))))]

    [:section {:class "col"}
     [:h5 {:title "This implies that there are 'least favourite people (to work with)', and we don't go there."} "Favourite people (to work with)"]

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

(defn- person-content [stored-state]
  [:div
   [:a {:href "#" :on-click #(swap! stored-state assoc :selected-key nil)} "back"]
   [:br]
   [:br]
   [:div {:class "row"}
    [:h4 {:class "col-sm-8"}
     (:selected-key @stored-state)]]

   [:div {:class "row"}
    [popular-songs (:selected-key @stored-state)]
    [popular-partners stored-state (:selected-key @stored-state)]
    [popular-roles (:selected-key @stored-state)]]])

(defn- song-badges [badges]
  [:section {:class "col"}
   [:div {:class "list-group"}
    (for [[year [[song-name freq i]]] badges]
      (let [bg-color (nth ["gold" "silver" "#cd7f32"] (dec i))
            fg-color (nth ["black" "black" "white"] (dec i))
            traditional-placement (nth ["Gold" "Silver" "Bronze"] (dec i))
            placement (nth ["Most-Used" "Second-Most-Used" "Third-Most-Used"] (dec i))]
        ^{:key year}
        [:span
         {:class ["badge" "rounded-pill"]
          :style {:backgroundColor bg-color
                  :color fg-color}
          :title (str "Out of all songs used in " year ", " song-name " was the " (.toLowerCase placement) " (used " freq " times).")}
         year " " traditional-placement]))]])

(defn- song-content [stored-state]
  [:div
   [:a {:href "#" :on-click #(swap! stored-state assoc :selected-key nil)} "back"]
   [:br]
   [:br]
   [:div {:class "row"}
    [:h4 {:class "col-sm-8"}
     (:selected-key @stored-state)]]

   (let [{:keys [history favourites]} (data/song-data (:selected-key @stored-state) data/entries)
         badges (filter #(not (empty? (second %))) (data/top-songs-by-year-badges (:selected-key @stored-state)))]
     [:div {:class "row"}
      [song-history stored-state history]
      [song-faves favourites]
      (when (not (empty? badges))
        [song-badges badges])])])

(defn- content [stored-state]
  [:section {:class "col-sm-10"}

    (case (:tab @stored-state)
      :people [:div {:class "col"}
               (when (nil? (:selected-key @stored-state))
                 [:input {:type "search"
                          :class "form-control"
                          :placeholder "Search people"
                          :on-input #(reset! simple-query (-> % .-target .-value))}])
               (if (nil? (:selected-key @stored-state))
                [people-content stored-state]
                [person-content stored-state])]
      :songs [:div {:class "col"}
              (when (nil? (:selected-key @stored-state))
                [:input {:type "search"
                         :class "form-control"
                         :placeholder "Search songs"
                         :on-input #(reset! simple-query (-> % .-target .-value))}])
              (if (nil? (:selected-key @stored-state))
               [songs-content stored-state]
               [song-content stored-state])]
      :roles [:p "Roles!"]
      :history [:div {:class "col"}
                [search-bar/component stored-state]
                [history-content stored-state]]

      [:p {:class "text-danger"} "Unknown tab = " (:tab @stored-state)])])

(defn component []
  (let [url-state (url->state js/window.location)
        stored-state (if (nil? (:tab url-state))
                       (r/atom {:tab :history
                                :selected-key nil
                                :sort {:dir :asc
                                       :k nil}
                                :query {:people #{}
                                        :songs #{}
                                        :roles #{}
                                        :starting-date nil
                                        :ending-date nil}})
                       (r/atom url-state))]

    (fn []
      [:section {:class "row"}
       [tabs-component stored-state]
       [content stored-state]])))
