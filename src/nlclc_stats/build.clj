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

(defn- sidebar-tab [active-tab tab-kw]
  [:li.nav-item
   [:a
    {:href (str (name tab-kw) ".html")
     :class ["nav-link" (when (= active-tab tab-kw) "active")]}
    (name tab-kw)]])

(defn- sidebar [active-tab]
  [:ul.nav.flex-column.nav-pills.col-sm-2
   (sidebar-tab active-tab :people)
   (sidebar-tab active-tab :songs)
   (sidebar-tab active-tab :history)])

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
    [:body.bg-dark {:data-bs-theme "dark"}
     header
     [:article.container content]]))

(def index
  (template
    [:section.row
     (sidebar :index)
     [:section.col-sm-10
      [:form
       {:action "history"
        :method "GET"
        :style {:margin "auto"}}
       [:label.form-label.mb-3 "Search"
        [:input.form-control {:name "query"}]]]]]))

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
       [:td (count (distinct (apply concat (map :entry/songs data/entries))))]]
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

(defn- create-index [output-dir]
  (let [index-file (io/file output-dir "index.html")]
    (printf "writing to %s\n" (.getName index-file))
    (spit index-file index)))

(defn- create-about [output-dir]
  (let [about-file (io/file output-dir "about.html")]
    (printf "writing to %s\n" (.getName about-file))
    (spit about-file about)))

(defn -main
  ([] (println "missing destination parameter"))
  ([path]
   (try
     (let [output-dir (io/file path)]
       (when (not (.exists output-dir))
         (printf "creating output directory `%s`\n" (.getName output-dir))
         (when (not (.mkdir output-dir))
           (throw (ex-info "could not create output directory" {:dirname (.getName output-dir)}))))

       (create-index output-dir)
       (create-about output-dir))
     (catch Exception e
       (printf "exception: %s with data %s\n" e (ex-data e))))))
