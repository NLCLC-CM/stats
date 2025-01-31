(ns nlclc-stats.data-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.string :as st]
            [nlclc-stats.data :as data]))

(deftest data-spec-validity
  (testing "entries meet the specs"
    (is (let [dates (map :entry/date data/entries)]
          (= (sort dates) dates)))

    (is (s/valid? :roster/entries data/entries)
        (s/explain-str :roster/entries data/entries)))

  (testing "songs should not have alternate version"
    (let [unique-songs (set (distinct (flatten (map :entry/songs data/entries))))
          replaced (set (map #(st/replace % "你" "你") unique-songs))]
      (doseq [song unique-songs]
        (is (contains? replaced song))))))
