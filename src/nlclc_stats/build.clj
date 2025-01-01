(ns nlclc-stats.build
  (:require [hiccup2.core :as h]
            [hiccup.page :as p]
            [clojure.java.io :as io]
            [nlclc-stats.data :as data]))

(def header
  [:header.container
   [:nav.navbar
    [:h1.navbar-brand "NLCLC Stats"]
    [:a.navbar-item
     {:href "about.html"}
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
   (sidebar-tab active-tab :people "/people/")
   (sidebar-tab active-tab :songs "/songs/")
   (sidebar-tab active-tab :history "/")])

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
            {:href (str "/people/" n ".html")}
            n])])]]

    [:script {:type "module" :src "/js/people.mjs"}]))

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

    [:script {:type "module" :src "/js/songs.mjs"}]))

(defn entry-role [role assoc-ppl]
  [:li
   [:span.role (name role)]
   [:span.people
    (for [person assoc-ppl]
      [:span.person person])]])

(defn entry-people [people]
  (let [people (sort-by first (into [] people))]
    [:ul.entry-people
     (for [[role associated-ppl] people]
       (entry-role role associated-ppl))]))

(defn entry-songs [songs]
  [:ul.entry-songs
   (for [song songs]
     [:li song])])

(defn entry [{:entry/keys [people date songs lecture-name]}]
  [:div.entry
   [:time {:datetime date} date]
   (entry-people people)
   (entry-songs songs)
   [:p.lecture-name
    lecture-name]])

(def index
  (template
    [:section.row
     (sidebar :history)
     [:section.col-sm-10
      [:form
       {:action "history"
        :method "GET"
        :style {:margin "auto"}}
       [:label.form-label.mb-3 "Search"
        [:input#query.form-control {:type "search"}]]]
      
      [:div#entries
       (for [e data/entries]
         (entry e))]]]))

(def about
  (template
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
         (:entry/date (last data/entries))]]]]]))

(defn- create-page
  ([output-dir filename filedata]
   (let [file (apply io/file output-dir filename)
         display-name (subs (.getAbsolutePath file) (count (.getAbsolutePath output-dir)))]
     (printf "writing to %s\n" display-name)
     (spit file filedata))))

(def pages
  `((["index.html"] ~index)
    (["about.html"] ~about)
    (["people" "index.html"] ~people)
    (["songs" "index.html"] ~songs)))

(defn- create-dir [dir]
  (when (not (.exists dir))
    (printf "creating output directory %s\n" (.getName dir))
    (when (not (.mkdir dir))
      (throw (ex-info "could not create output directory" {:dirname (.getName dir)})))))

(defn -main
  ([] (println "missing destination parameter"))
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
       (printf "exception: %s with data %s\n" e (ex-data e))))))
