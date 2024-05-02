(ns main.nlclc-stats.data
  (:require [cljs.spec.alpha :as s]
            [main.nlclc-stats.data.year2021]
            [main.nlclc-stats.data.year2022]
            [main.nlclc-stats.data.year2023]
            [main.nlclc-stats.data.year2024]))

(def names
  #{
    "Adriel"
    "Amy"
    "Andrew"
    "April"
    "Arthur"
    "Bertha"
    "Billy Sir"
    "Cheuk"
    "Cindy"
    "Dave"
    "Devin"
    "Dorothy"
    "Eddie"
    "Eric"
    "Esther"
    "Gab"
    "Gabe"
    "Gigi"
    "Helen"
    "Jack"
    "Jesse"
    "Joyce"
    "Julianna"
    "Ka Bo"
    "Ken"
    "Kiki Pau"
    "Kiki"
    "Kovia"
    "Liberty"
    "Martin"
    "Matthew"
    "Mavis"
    "Philson"
    "Rita"
    "Sammy"
    "Sinead"
    "Stephy"
    "Steven"
    "Terry"
    "Tin Hei"
    "Tin Lok"
    "ToTo"
    "Toby"
    "Vivian"
    "YauYau"
    "Yoko"

    "飛宇"
    })

(def roles #{:lead
             :vocals
             :pianist
             :drummer
             :bassist
             :guitarist
             :av
             :usher})

(s/def :entry/person (s/and string? (partial contains? names)))

(s/def :entry/date (s/and string? (partial re-matches #"^[0-9]{4}-[0-9]{2}-[0-9]{2}$")))

(s/def :entry/role (s/and keyword? (partial contains? roles)))

(s/def :entry/people (s/map-of :entry/role (s/coll-of :entry/person)))

(s/def :entry/songs (s/coll-of string?))

(s/def :entry/lecture-name string?)

(s/def :roster/entry (s/keys :req [:entry/date :entry/people :entry/songs :entry/lecture-name]
                             :opt []))

(s/def :roster/entries (s/coll-of :roster/entry))

(def entries
  (concat
    main.nlclc-stats.data.year2021/assignments
    main.nlclc-stats.data.year2022/assignments
    main.nlclc-stats.data.year2023/assignments
    main.nlclc-stats.data.year2024/assignments))

(defn- assoc-person-data [m e person]
  (if (contains? m person)
    (assoc-in (update-in m [person :frequency] inc) [person :ending-date] (:entry/date e))
    (assoc m person {:starting-date (:entry/date e)
                     :ending-date (:entry/date e)
                     :frequency 1})))

(defn- assoc-people-data [m e blacklisted-roles]
  (loop [[person & people :as remaining] (flatten (vals (apply dissoc (:entry/people e) blacklisted-roles)))
         m m]
    (if (empty? remaining)
      m
      (recur
        people
        (assoc-person-data m e person)))))

(defn songs-frequencies
  "Get songs data. Return a map of song name and their occurrance frequency."
  [entries]
  (->> entries
       (map :entry/songs)
       flatten
       frequencies))

(defn people-data
  "Get people data."
  [entries blacklisted-roles]
  (loop [m {}
         [e & ex :as remaining] entries]
    (if (empty? remaining)
      m
      (recur (assoc-people-data m e blacklisted-roles) ex))))

(defn people-frequencies
  "Get people frequencies. Return a map of person name and their occurrance frequency."
  [entries blacklisted-roles]
  (->> entries
       (map (comp (partial into []) :entry/people))
       (apply concat)
       (filter #(not (contains? blacklisted-roles (first %))))
       (map second)
       flatten
       frequencies))

(defn song-history
  [song entries]
  (->> entries
       (filter (comp #(contains? % song) set :entry/songs))
       (map :entry/date)))

(defn song-pairings
  [song entries]
  (as-> entries $
       (filter (comp #(contains? % song) set :entry/songs) $)
       (songs-frequencies $)
       (dissoc $ song)))

(defn song-data
  "Get song data. Includes when it was used in history, as well as the songs most used."
  [song entries]
  {:history (song-history song entries)
   :favourites (->> (song-pairings song entries)
                    (into [])
                    (sort-by second)
                    reverse)})

(defn roles-frequencies
  "Get roles frequencies. Return a map of role name and their occurrance frequency."
  [entries person]
  (->> entries
       (map (comp (partial into []) :entry/people))
       (apply concat)
       (filter #(contains? (set (second %)) person))
       (map first)
       flatten
       frequencies))
