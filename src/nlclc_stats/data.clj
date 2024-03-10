(ns nlclc-stats.data
  (:require [clojure.spec.alpha :as s]))

(def names #{"Gabriel Lam"
             "Helen"
             "Phison"
             "Adriel"
             "Andrew"
             "Cheuk Yin"
             "Michelle To"})

(def roles #{:worship-lead
             :vocals
             :pianist
             :drummer
             :bassist
             :guitarist})

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
