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

(defn- submit-element []
  :hello)

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
        (swap! people cons @query)

        "songs"
        (swap! songs cons @query)

        "roles"
        (swap! roles cons @query)

        "starting-date"
        (reset! starting-date @query)

        "ending-date"
        (reset! ending-date @query))

      (reset! query "")))

(defn- change-select [this]
  (do (reset! selected-item (-> this .-target .-value))
      (reset! query "")))

(defn- add-component []
  [:div {:class "input-group mb-3"}
   [:select {:class "btn btn-outline-secondary"
             :value @selected-item
             :on-change change-select}
    [:option {:value "people"} "People"]
    [:option {:value "songs"} "Songs"]
    [:option {:value "roles"} "Roles"]
    [:option {:value "starting-date"} "Starting date"]
    [:option {:value "ending-date"} "Ending date"]]

   ; TODO change input based on what we select
   [:input {:type "text"
            :class "form-control"
            :list "autocomplete-list"
            :value @query   ; TODO add validation on input
            :on-input #(reset! query (-> % .-target .-value))}]
   [autocomplete-list]

   [:button {:on-click add :class "btn btn-outline-secondary"}
    "Add"]])

(defn- delete-entity-btn [entity entity-ls]
  [:button {:on-click #(swap! entity-ls filter (partial not= entity))
            :title "Remove from query"
            :class "btn btn-outline-secondary"}
   entity])

(defn- delete-scalar-btn [scalar]
  [:button {:on-click #(reset! scalar nil)
            :title "Remove from query"
            :class "btn btn-outline-secondary"}
   @scalar])

(defn- query-explanation [thing-to-search]
  "Assumes the query isn't empty."
  `[:p "Searching for " ~thing-to-search " with "

    ~@(when (not (empty? @people))
        (cons "people"
              (for [person @people]
                [delete-entity-btn person people])))

    ~@(when (not (empty? @songs))
        (cons "songs"
              (for [song @songs]
                [delete-entity-btn song songs])))

    ~@(when (not (empty? @roles))
        (cons "roles"
              (for [role @roles]
                [delete-entity-btn role roles])))

    ~@(when (not (nil? @starting-date))
        (cons "starting on"
              [delete-scalar-btn starting-date]))

    ~@(when (not (nil? @ending-date))
        (cons "ending on"
              [delete-scalar-btn ending-date]))])

(defn component [thing-to-search]
  [:section {:class "col"}
   [add-component]
   (when (not (and (empty? @people)
                   (empty? @songs)
                   (empty? @roles)
                   (nil? @starting-date)
                   (nil? @ending-date)))
     [query-explanation thing-to-search])])
