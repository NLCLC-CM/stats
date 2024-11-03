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
   (sidebar-tab active-tab :people)
   (sidebar-tab active-tab :songs)
   (sidebar-tab active-tab :history "index.html")])

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
      (for [names (partition 4 (sort data/names))]
        [:div.row.mb-3
         (for [n names]
           [:a.col.name
            {:href (str "/people/" n ".html")}
            n])])]]))

(def songs
  (template
    [:section.row
     (sidebar :songs)
     [:section.col-sm-10
      [:label.form-label.mb-3 "Search"
       [:input#query.form-control {:type "search"}]]

      (for [songs-list (partition 4 (sort distinct-songs))]
        [:div.row.mb-3
         (for [s songs-list]
           [:a.col.song
            {:href (str "/songs/" s ".html")}
            s])])]]

    (p/include-js "/js/songs.js")))

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
        [:input#query.form-control {:type "search"}]]]]]))

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
   (let [file (io/file output-dir filename)]
     (printf "writing to %s\n" (.getName file))
     (spit file filedata)))
  ([output-dir filename filedata js-filename]
   (let [src (io/file "public" "js" js-filename)
         dest-parent (io/file output-dir "js")
         dest (io/file output-dir "js" js-filename)]
     (when (not (.exists dest-parent))
       (printf "creating js directory %s\n" (.getName dest-parent))
       (.mkdirs dest-parent))

     (printf "copying to %s\n" (.getName dest))
     (io/copy src dest)

     (create-page output-dir filename filedata))))

(def pages
  `(("index.html" ~index)
    ("about.html" ~about)
    ("people.html" ~people)
    ("songs.html" ~songs "songs.js")))

(defn -main
  ([] (println "missing destination parameter"))
  ([path]
   (try
     (let [output-dir (io/file path)]
       (when (not (.exists output-dir))
         (printf "creating output directory `%s`\n" (.getName output-dir))
         (when (not (.mkdir output-dir))
           (throw (ex-info "could not create output directory" {:dirname (.getName output-dir)}))))

       (doseq [page-data pages]
         (apply create-page output-dir page-data)))
     (catch Exception e
       (printf "exception: %s with data %s\n" e (ex-data e))))))
