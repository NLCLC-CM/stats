(ns nlclc-stats.data-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [nlclc-stats.data :as data]))

(deftest data-spec-validity
  (testing "entries meet the specs"
    (is (let [dates (map :entry/date data/entries)]
          (= (sort dates) dates)))

    (is (s/valid? :roster/entries data/entries)
        (s/explain-str :roster/entries data/entries))))

