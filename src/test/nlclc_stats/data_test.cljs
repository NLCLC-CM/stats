(ns test.nlclc-stats.data-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [cljs.spec.alpha :as s]
            [main.nlclc-stats.data :as data]))

(deftest data-spec-validity
  (testing "entries meet the specs"
    (is (let [dates (map :entry/date data/entries)]
          (= (sort dates) dates)))

    (is (s/valid? :roster/entries data/entries)
        (s/explain-str :roster/entries data/entries))))
