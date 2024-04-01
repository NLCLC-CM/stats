(ns main.nlclc-stats.component.search-bar
  (:require [reagent.core :as r]
            [main.nlclc-stats.data :as data]))

(def ^:const DISTINCTSONGS (distinct (apply concat (map :entry/songs data/entries))))

(defonce selected-item (r/atom "songs"))
(defonce query (r/atom ""))
(defonce people (r/atom []))
(defonce songs (r/atom []))
(defonce roles (r/atom []))
(defonce starting-date (r/atom nil))
(defonce ending-date (r/atom nil))

(defn- get-query []
  {:people @people
   :songs @songs
   :roles @roles
   :starting-date @starting-date
   :ending-date @ending-date})

(defn- autocomplete-list []
  `[:datalist {:id "autocomplete-list"}
    ~@(case @selected-item
        "people"
        (for [person-name (into '() data/names)]
          [:option {:value person-name} person-name])

        "songs"
        (for [song-name DISTINCTSONGS]
          [:option {:value song-name} song-name])

        (list @selected-item))])

(defn- add []
  (do (case @selected-item
        "people"
        (swap! people conj @query)

        "songs"
        (swap! songs conj @query)

        "roles"
        (swap! roles conj @query)

        "starting-date"
        (reset! starting-date @query)

        "ending-date"
        (reset! ending-date @query))

      (reset! query "")))

(defn- change-select [this]
  (do (reset! selected-item (-> this .-target .-value))
      (reset! query "")))

(defn- input []
  (case @selected-item
    ("people" "songs" "roles")
    [:div {:class "form-control"}
     [:input {:type "text"
              :class "form-control"
              :list "autocomplete-list"
              :value @query   ; TODO add validation on input
              :on-input #(reset! query (-> % .-target .-value))}]
     [autocomplete-list]]

    ("starting-date" "ending-date")
    [:input {:type "date"
             :class "form-control"
             :value @query
             :on-input #(reset! query (-> % .-target .-value))}]))

(defn- add-component [on-change]
  [:div {:class "input-group mb-3"}
   [:select {:class "btn btn-outline-secondary"
             :value @selected-item
             :on-change change-select}
    [:option {:value "people"} "People"]
    [:option {:value "songs"} "Songs"]
    [:option {:value "roles"} "Roles"]
    [:option {:value "starting-date"} "Starting date"]
    [:option {:value "ending-date"} "Ending date"]]

   [input]

   [:button {:on-click #(do (add)
                            (on-change (get-query)))
             :class "btn btn-outline-secondary"}
    "Add"]])

(defn- delete-entity-btn [{:keys [on-change x xs]}]
  [:button {:on-click #(do (reset! xs (remove (partial = x) @xs))
                           (on-change (get-query)))
            :title "Remove from query"
            :style {:margin-left "0.5rem" :margin-right "0.5rem"}
            :class "btn btn-outline-secondary"}
   x])

(defn- delete-scalar-btn [{:keys [on-change x]}]
  [:button {:on-click #(do (reset! x nil)
                           (on-change (get-query)))
            :title "Remove from query"
            :class "btn btn-outline-secondary"}
   @x])

(defn- query-explanation [{:keys [thing-to-search on-change]}]
  "Assumes the query isn't empty."
  `[:p "Searching for " ~thing-to-search " with "

    ~@(when (not (empty? @people))
        (cons "people"
              (for [person @people]
                [delete-entity-btn {:on-change on-change
                                    :x person
                                    :xs people}])))

    ~@(when (not (empty? @songs))
        (cons "songs"
              (for [song @songs]
                [delete-entity-btn {:on-change on-change
                                    :x song
                                    :xs songs}])))

    ~@(when (not (empty? @roles))
        (cons "roles"
              (for [role @roles]
                [delete-entity-btn {:on-change on-change
                                    :x role
                                    :xs roles}])))

    ~@(when (not (nil? @starting-date))
        (cons "starting on"
              [delete-scalar-btn {:on-change on-change
                                  :x starting-date}]))

    ~@(when (not (nil? @ending-date))
        (cons "ending on"
              [delete-scalar-btn {:on-change on-change
                                  :x ending-date}]))])

(defn component [{:keys [thing-to-search on-change]}]
  [:section {:class "col"}
   [add-component on-change]
   (when (not (and (empty? @people)
                   (empty? @songs)
                   (empty? @roles)
                   (nil? @starting-date)
                   (nil? @ending-date)))
     [query-explanation {:thing-to-search thing-to-search
                         :on-change on-change}])])
