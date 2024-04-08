(ns main.nlclc-stats.component.search-bar
  (:require [reagent.core :as r]
            [clojure.set :refer [difference]]
            [main.nlclc-stats.data :as data]))

(def ^:const DISTINCTSONGS (set (apply concat (map :entry/songs data/entries))))

(defonce selected-item (r/atom "songs"))
(defonce query (r/atom ""))
(defonce people (r/atom #{}))
(defonce songs (r/atom #{}))
(defonce roles (r/atom #{}))
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
        (for [person-name (difference data/names @people)]
          [:option {:value person-name} person-name])

        "songs"
        (for [song-name (difference DISTINCTSONGS @songs)]
          [:option {:value song-name} song-name])

        "roles"
        (for [role-name (difference data/roles @roles)]
          [:option {:value (name role-name)} (name role-name)])

        (list))])

(defn add-item [item-type text]
  (do (case item-type
        "people"
        (swap! people conj text)

        "songs"
        (swap! songs conj text)

        "roles"
        (swap! roles conj text)

        "starting-date"
        (reset! starting-date text)

        "ending-date"
        (reset! ending-date text))))

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

   [input]

   [:button {:on-click #(do (add-item @selected-item @query)
                            (reset! query ""))
             :class "btn btn-outline-secondary"}
    "Add"]])

(defn- delete-entity-btn [{:keys [x xs]}]
  [:button {:on-click #(do (reset! xs (remove (partial = x) @xs)))
            :title "Remove from query"
            :style {:margin-left "0.5rem" :margin-right "0.5rem"}
            :class "btn btn-outline-secondary"}
   x])

(defn- delete-scalar-btn [{:keys [x]}]
  [:button {:on-click #(reset! x nil)
            :title "Remove from query"
            :style {:margin-left "0.5rem" :margin-right "0.5rem"}
            :class "btn btn-outline-secondary"}
   @x])

(defn- query-explanation [{:keys [thing-to-search]}]
  "Assumes the query isn't empty."
  `[:p "Searching for " ~thing-to-search " with "

    ~@(when (not (empty? @people))
        (cons "people"
              (for [person @people]
                [delete-entity-btn {:x person
                                    :xs people}])))

    ~@(when (not (empty? @songs))
        (cons "songs"
              (for [song @songs]
                [delete-entity-btn {
                                    :x song
                                    :xs songs}])))

    ~@(when (not (empty? @roles))
        (cons "roles"
              (for [role @roles]
                [delete-entity-btn {
                                    :x role
                                    :xs roles}])))

    ~@(when (not (nil? @starting-date))
        (cons "starting on"
              (list [delete-scalar-btn {
                                        :x starting-date}])))

    ~@(when (not (nil? @ending-date))
        (cons "ending on"
              (list [delete-scalar-btn {
                                        :x ending-date}])))])

(defn set-query! [init-query]
  (reset! people (:people init-query #{}))
  (reset! songs (:songs init-query #{}))
  (reset! roles (:roles init-query #{}))
  (reset! starting-date (:starting-date init-query))
  (reset! ending-date (:ending-date init-query)))

(defn component [{:keys [thing-to-search]}]
  [:section {:class "col"}
   [add-component]
   (when (not (and (empty? @people)
                   (empty? @songs)
                   (empty? @roles)
                   (nil? @starting-date)
                   (nil? @ending-date)))
     [query-explanation {:thing-to-search thing-to-search}])])
