(ns nlclc-stats.data
  (:require [clojure.spec.alpha :as s]))

(def names #{"Gab"
             "Helen"
             "Amy"
             "Terry"
             "Billy Sir"
             "Steven"
             "Gabe"
             "Philson"
             "Adriel"
             "Kiki"
             "Ken"
             "Dorothy"
             "Vivian"
             "Gigi"
             "Andrew"
             "Arthur"
             "Bertha"
             "Eddie"
             "YauYau"
             "Sinead"
             "Sammy"
             "Toby"
             "Kovia"
             "Cindy"
             "Stephy"
             "Martin"
             "Jack"
             "Kiki Pau"
             "Mavis"
             "Devin"
             "Matthew"
             "April"
             "Eric"
             "Ka Bo"
             "Cheuk"
             "ToTo"})

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

; (def valid-entries? (partial s/valid? :roster/entries))
