(ns nlclc-stats.build
  (:require [hiccup2.core :as h]
            [hiccup.page :as p]
            [clojure.tools.logging.readable :refer [info error errorf infof trace tracef]]
            [clojure.java.io :as io]
            [nlclc-stats.data :as data]))

(def ROOT
  (or (System/getenv "ROOT_FOLDER")
      "/"))

(defn make-stats
  []
  (defn- update-people-stats
    [people-stats date entry-people songs]
    people-stats)
; (update-song-stats {:people {"Cheuk" 2} :songs {"good song" 12} :dates #{"2025-02-01"}} "2025-01-01" ["Phils" "Cheuk"] ["abc-song" "good song"])

  (defn- update-song-stats
    [song-stats date entry-people songs]
    (defn- -inc
      [x]
      (if (nil? x)
        1
        (inc x)))

    (defn- -person
      [m person]
      (update-in m [:people person] -inc))

    (defn- -people
      [m people]
      (loop [m m
             [person & rst :as all] people]
        (if (empty? all)
          m
          (recur (-person m person)
                 rst))))

    (defn- -song
      [m song]
      (update-in m [:songs song] -inc))

    (defn- -songs
      [m songs]
      (loop [m m
             [song & rst :as all] songs]
        (if (empty? all)
          m
          (recur (-song m song)
                 rst))))

    (defn- f
      [m songs]
      (if (nil? m)
        {:people (into {} (map #(vector % 1) entry-people))
         :songs (into {} (map #(vector % 1) songs))
         :dates #{date}}
        (-> m
            (update :dates conj date)
            (-people entry-people)
            (-songs songs))))

    (loop [s song-stats
           [song & rst :as all] songs]
      (if (empty? all)
        s
        (recur (update s song f (remove (partial = song) songs))
               rst))))

  (defn- update-stats
    [{:keys [people-stats song-stats]} {:entry/keys [people songs date] :as entry}]
    (let [entry-people (-> people (dissoc :av) vals flatten distinct)]
      {:people-stats (update-people-stats people-stats date entry-people songs)
       :song-stats (update-song-stats song-stats date entry-people songs)}))

  (loop [s {:people-stats {} :song-stats {}}
         [e & es :as rst] data/entries]
    (if (empty? rst)
      s
      (recur (update-stats s e)
             es))))

(def stats (make-stats))
(def people-stats (:people-stats stats))
(def song-stats (:song-stats stats))

(defn ->abs-url [& parts]
  (apply str ROOT (interpose "/" parts)))

(def header
  [:header.container
   [:nav.navbar
    [:h1.navbar-brand "NLCLC Stats"]
    [:a.navbar-item
     {:href (->abs-url "about.html")}
     "About this site"]]])

(defn- sidebar-tab
  ([active-tab tab-kw]
   (sidebar-tab active-tab tab-kw (str (name tab-kw) ".html")))
  ([active-tab tab-kw href-tab]
   [:li.nav-item
    [:a
     {:href href-tab
      :class ["nav-link" (when (= active-tab tab-kw) "active")]}
     (name tab-kw)]]))

(defn- sidebar [active-tab]
  [:ul.nav.flex-column.nav-pills.col-sm-2
   (sidebar-tab active-tab :people (->abs-url "people/"))
   (sidebar-tab active-tab :songs (->abs-url "songs/"))
   (sidebar-tab active-tab :history (->abs-url ""))])

(defn- template [& content]
  (p/html5
    [:head
     [:title "NLCLC Stats"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
             :integrity "sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
             :crossorigin "anonymous"}]
     [:script {:type "text/javascript"
               :src "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
               :integrity "sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
               :crossorigin "anonymous"}]]
    [:body.bg-dark
     {:data-bs-theme "dark"
      :style {:text-transform "uppercase"}}
     header
     [:article.container content]]))

(def distinct-songs
  (->> data/entries
       (map :entry/songs)
       (apply concat)
       (distinct)))

(def people
  (template
    [:section.row
     (sidebar :people)
     [:section.col-sm-10
      [:label.form-label.mb-3 "Search"
       [:input#query.form-control {:type "search"}]]

      (for [names (partition 4 (sort data/names))]
        [:div.row
         (for [n names]
           [:a.col-3.name.mb-3
            {:href (->abs-url (str "people/" n ".html"))}
            n])])]]

    [:script {:type "module" :src (->abs-url "js" "people.mjs")}]))

(def songs
  (template
    [:section.row
     (sidebar :songs)
     [:section.col-sm-10
      [:label.form-label.mb-3 "Search"
       [:input#query.form-control {:type "search"}]]

      (for [songs-list (partition 4 (sort distinct-songs))]
        [:div.row
         (for [s songs-list]
           [:a.col-3.song.mb-3
            {:href (str "/songs/" s ".html")}
            s])])]]

    [:script {:type "module" :src (->abs-url "js" "songs.mjs")}]))

(defn entry-role [role assoc-ppl]
  [:li.entry-role
   [:span.role (name role)]
   [:span.people
    (for [person assoc-ppl]
      [:span.person person])]])

(defn entry-people [people]
  (let [people (sort-by first (into [] people))]
    [:ul.entry-people
     {:style {:display "none"}}
     (for [[role associated-ppl] people]
       (entry-role role associated-ppl))]))

(defn entry-songs [songs]
  [:ul.entry-songs
   {:style {:display "none"}}
   (for [song songs]
     [:li.song song])])

(def earliest-year
  (-> data/entries
      first
      :entry/date
      (subs 0 4)))

(def latest-year
  (-> data/entries
      last
      :entry/date
      (subs 0 4)))

(defn entry [{:entry/keys [people date songs lecture-name]}]
  (let [year (subs date 0 4)
        month (subs date 5 7)
        day (subs date 8)]
    [:div.entry.p-2
     [:time {:datetime date} (str month "/" day)]
     (entry-people people)
     (entry-songs songs)
     [:p.lecture-name
      lecture-name]
     [:button.details
      "Details"]]))

(def all-years
  (as-> data/entries $
        (map (comp #(subs % 0 4) :entry/date) $)
        (set $)
        (sort $)))

(def index
  (let [part-by-year (partition-by (comp #(subs % 0 4) :entry/date) data/entries)]
    (template
      [:section.row
       (sidebar :history)
       [:section.col-sm-10
        [:div#entries
         (for [entries part-by-year]
           (let [year (subs (:entry/date (first entries)) 0 4)
                 part-by-month (partition-by (comp #(subs % 5 7) :entry/date) entries)]
             [:div.year
              {:data-year year
               :style (if (= year latest-year) {} {:display "none"})}
              [:h2
               [:button.prev.btn.btn-outline-primary.me-3
                (when (= year earliest-year) {:disabled true})
                "<"]
               [:select.year-selector
                {:data-year year}
                (for [y all-years]
                  [:option (if (= year y) {:value y :selected true} {:value y}) y])]
               [:button.next.btn.btn-outline-primary.ms-3
                (when (= year latest-year) {:disabled true})
                ">"]]
              (for [month-entries part-by-month]
                [:div.month.d-flex.flex-row
                 (for [e month-entries]
                   (entry e))])]))]]]

      [:dialog#details-box
       {:style {:min-width "30rem"}}
       [:form
        {:method "dialog"}
        [:input {:type "submit" :value "Close"}]]
       [:table.table.table-hover
        [:tbody
         {:style {:text-transform "none"}}
         [:tr
          [:td "Date"]
          [:td [:time#selected-time ""]]]
         [:tr
          [:td "Sermon title"]
          [:td#selected-sermon-title ""]]
         [:tr
          [:td "Participants"]
          [:td#selected-participants ""]]
         [:tr
          [:td "Songs"]
          [:td#selected-songs.d-flex.flex-column ""]]]]]

      [:link {:href (->abs-url "css" "index.css") :rel "stylesheet"}]
      [:script {:type "module" :src (->abs-url "js" "index.mjs")}])))

(def about
  (template
   [:div
    {:style {:text-transform "none"}}

    [:h1 "About this site"]
    [:p "This site was created with the following goals in mind:"]
    [:ol
     [:li "To recommend songs based on frequency"]
     [:li "To view most-likely groupings"]
     [:li "To gamify the act of serving"]]
    [:p "Safe to say that these goals are met."]
    [:p
     "Code: "
     [:a {:href "https://github.com/NLCLC-CM/stats"}
      "https://github.com/NLCLC-CM/stats"]]

    [:table.table
     [:thead
      [:tr
       [:th {:scope "col"} "Site statistics"]
       [:th {:scope "col"} "Data"]]]
     [:tbody
      [:tr
       [:th {:scope "row"} "Entries"]
       [:td (count data/entries)]]
      [:tr
       [:th {:scope "row"} "People"]
       [:td (count data/names)]]
      [:tr
       [:th {:scope "row"} "Songs"]
       [:td (count distinct-songs)]]
      [:tr
       [:th {:scope "row"} "Earliest entry"]
       [:td
        [:time
         {:datetime (:entry/date (first data/entries))}
         (:entry/date (first data/entries))]]]
      [:tr
       [:th {:scope "row"} "Latest entry"]
       [:td
        [:time
         {:datetime (:entry/date (last data/entries))}
         (:entry/date (last data/entries))]]]]]]))

(defn- create-page
  ([output-dir filename filedata]
   (let [file (apply io/file output-dir filename)
         display-name (subs (.getAbsolutePath file) (count (.getAbsolutePath output-dir)))]
     (tracef "writing to %s" display-name)
     (spit file filedata))))

(def pages
  `((["index.html"] ~index)
    (["about.html"] ~about)
    (["people" "index.html"] ~people)
    (["songs" "index.html"] ~songs)))

(defn- create-dir [dir]
  (when (not (.exists dir))
    (tracef "creating output directory %s" (.getName dir))
    (when (not (.mkdir dir))
      (throw (ex-info "could not create output directory" {:dirname (.getName dir)})))))

(defn -main
  ([] (error "missing destination parameter"))
  ([path]
   (try
     (let [output-dir (io/file path)
           people-dir (io/file output-dir "people")
           songs-dir (io/file output-dir "songs")]

       (create-dir output-dir)
       (create-dir people-dir)
       (create-dir songs-dir)

       (doseq [page-data pages]
         (apply create-page output-dir page-data)))
     (catch Exception e
       (error e "something went wrong??")))))
