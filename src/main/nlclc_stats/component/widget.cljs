(ns main.nlclc-stats.component.widget
  (:require [reagent.core :as r]))

(defonce tab (r/atom :history))
(defonce search (r/atom []))
(defonce selected-key (r/atom nil))

(defn- switch-tabs! [new-tab-name]
  (do
    (reset! tab new-tab-name)
    (reset! search [])
    (reset! selected-key nil)))

(defn- tabs-component []
  (let [props-for (fn [t] {:class ["nav-link" "nav-item" (when (= t @tab) "active")]
                           :on-click #(switch-tabs! t)})]
    [:ul {:class "nav flex-column nav-pills col-2"}
     [:li (props-for :people) "People"]
     [:li (props-for :songs) "Songs"]
     [:li (props-for :roles) "Roles"]
     [:li (props-for :history) "History"]
     ]))

(defn- history-content []
  [:p "History!"])

(defn- content []
  [:section {:class "col-10"}
   (case @tab
     :history [history-content]

     [:p {:class "text-danger"} "Unknown tab = " @tab])])

(defn component []
  [:section {:class "row"}
   [tabs-component]
   [content]])