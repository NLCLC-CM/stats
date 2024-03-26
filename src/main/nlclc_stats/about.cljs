(ns main.nlclc-stats.about
  (:require [main.nlclc-stats.data :as data]))

(defn init []
  (let [entry-count-elem (js/document.getElementById "num-entries")
        num-people-elem (js/document.getElementById "num-people")
        num-songs-elem (js/document.getElementById "num-songs")
        latest-date-elem (js/document.getElementById "latest-entry")
        earliest-date-elem (js/document.getElementById "earliest-entry")
        num-songs (count (distinct (apply concat (map :entry/songs data/entries))))
        earliest-entry (apply min-key :entry/date data/entries)
        latest-entry (apply max-key :entry/date data/entries)]
    (set! (.-textContent entry-count-elem) (count data/entries))
    (set! (.-textContent num-people-elem) (count data/names))
    (set! (.-textContent num-songs-elem) num-songs)
    (set! (.-textContent earliest-date-elem) (:entry/date earliest-entry))
    (set! (.-textContent latest-date-elem) (:entry/date latest-entry))))
