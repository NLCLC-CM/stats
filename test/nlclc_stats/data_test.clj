(ns nlclc-stats.data-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.string :as st]
            [nlclc-stats.data :as data]))

(deftest data-validity
  (testing "entries meet the specs"
    (is (let [dates (map :entry/date data/entries)]
          (= (sort dates) dates)))

    (doseq [entry data/entries]
      (is (s/valid? :roster/entry entry)
          (s/explain-str :roster/entry entry)))

  (testing "songs should not have alternate version"
    (let [unique-songs (set (distinct (flatten (map :entry/songs data/entries))))
          replaced (set (map #(st/replace % "你" "你") unique-songs))]
      (doseq [song unique-songs]
        (is (contains? replaced song)))))
  
  (testing "unique lecture names (might delete later)"
    (let [lectures (map :entry/lecture-name data/entries)
          freq (frequencies lectures)]
      (doseq [[lecture-name c] (into [] freq)]
        (is (or (= lecture-name "n/a") (= 1 c))
            lecture-name))))))
