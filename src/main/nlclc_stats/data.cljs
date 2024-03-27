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
