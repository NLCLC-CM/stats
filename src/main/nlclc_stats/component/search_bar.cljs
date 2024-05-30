(ns main.nlclc-stats.component.search-bar
  (:require [reagent.core :as r]
            [clojure.set :refer [difference]]
            [main.nlclc-stats.data :as data]))

(def ^:const DISTINCTSONGS (set (apply concat (map :entry/songs data/entries))))

(defn- autocomplete-list [query selected-item]
  [:datalist {:id "autocomplete-list"}
   (case selected-item
     "people"
     (for [person-name (difference data/names (:people query))]
       ^{:key person-name} [:option {:value person-name} person-name])

     "songs"
     (for [song-name (difference DISTINCTSONGS (:songs query))]
       ^{:key song-name} [:option {:value song-name} song-name])

     "roles"
     (for [role-name (difference data/roles (:roles query))]
       ^{:key role-name} [:option {:value (name role-name)} (name role-name)])

     (list))])

(defn add-item [item-type text stored-state]
  (do (case item-type
        "people"
        (swap! stored-state update-in [:query :people] conj text)

        "songs"
        (swap! stored-state update-in [:query :songs] conj text)

        "roles"
        (swap! stored-state update-in [:query :roles] conj text)

        "starting-date"
        (when (not (empty? text))
          (swap! stored-state assoc-in [:query :starting-date] text))

        "ending-date"
        (when (not (empty? text))
          (swap! stored-state assoc-in [:query :ending-date] text)))))

(defn- input [{:keys [query selected-item text on-input]}]
  (case selected-item
    ("people" "songs" "roles")
    (let [query (r/atom "")]
      [:div {:class "form-control"}
       [:input {:type "search"
                :class "form-control"
                :placeholder "Search (autocomplete)"
                :list "autocomplete-list"
                :on-input on-input}]
       [autocomplete-list query selected-item]])

    ("starting-date" "ending-date")
    [:input {:type "date"
             :class "form-control"
             :on-input on-input}]))

(defn- add-component [stored-state]
  (let [selected-item (r/atom "songs")
        typed-text (r/atom "")]
    (fn []
      [:div {:class "input-group mb-3"}
       [:select {:class "btn btn-outline-secondary"
                 :on-change #(reset! selected-item (-> % .-target .-value))}
        [:option {:value "people"} "People"]
        [:option {:value "songs"} "Songs"]
        [:option {:value "roles"} "Roles"]
        [:option {:value "starting-date"} "Starting date"]
        [:option {:value "ending-date"} "Ending date"]]

       [input {:on-input #(reset! typed-text (-> % .-target .-value))
               :query (:query @stored-state)
               :selected-item @selected-item}]

       [:button {:on-click #(add-item @selected-item @typed-text stored-state)
                 :class "btn btn-outline-secondary"}
        "Add"]])))

(defn- delete-btn [{:keys [x on-click]}]
  [:button {:on-click on-click
            :title "Remove from query"
            :style {:margin-left "0.5rem" :margin-right "0.5rem"}
            :class "btn btn-outline-secondary"}
   x])

(defn- query-explanation [stored-state]
  "Assumes the query isn't empty."
  [:p "Searching for " (:tab @stored-state) " with "

   (when (not (empty? (get-in @stored-state [:query :people])))
     (cons "people"
           (for [person (get-in @stored-state [:query :people])]
             ^{:key person}
             [delete-btn {:x person
                          :on-click #(swap! stored-state update-in [:query :people] disj person)}])))

   (when (not (empty? (get-in @stored-state [:query :songs])))
     (cons "songs"
           (for [song (get-in @stored-state [:query :songs])]
             ^{:key song}
             [delete-btn {:x song
                          :on-click #(swap! stored-state update-in [:query :songs] disj song)}])))

   (when (not (empty? (get-in @stored-state [:query :roles])))
     (cons "roles"
           (for [role (get-in @stored-state [:query :roles])]
             ^{:key role}
             [delete-btn {:x role
                          :on-click #(swap! stored-state update-in [:query :roles] disj role)}])))

   (when (not (nil? (get-in @stored-state [:query :starting-date])))
     (cons "starting on"
           (list [delete-btn {:on-click #(swap! stored-state assoc-in [:query :starting-date] nil)
                              :x (get-in @stored-state [:query :starting-date])}])))

   (when (not (nil? (get-in @stored-state [:query :ending-date])))
     (cons "ending on"
           (list [delete-btn {:on-click #(swap! stored-state assoc-in [:query :ending-date] nil)
                              :x (get-in @stored-state [:query :ending-date])}])))])

(defn- need-explanation? [query]
  (not (and (empty? (:people query))
            (empty? (:songs query))
            (empty? (:roles query))
            (nil? (:starting-date query))
            (nil? (:ending-date query)))))

(defn component [stored-state]
  [:section {:class "col"}
   [add-component stored-state]
   (when (need-explanation? (:query @stored-state))
     [query-explanation stored-state])])
