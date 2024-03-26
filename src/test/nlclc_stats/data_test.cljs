(ns test.nlclc-stats.data-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [cljs.spec.alpha :as s]
            [main.nlclc-stats.data :as data]
            [main.nlclc-stats.data.year2022]
            [main.nlclc-stats.data.year2023]
            [main.nlclc-stats.data.year2024]
            ))

(deftest data-spec-validity
  (testing "2024"
    (is (s/valid? :roster/entries nlclc-stats.data.year2024/assignments)
        (s/explain-str :roster/entries nlclc-stats.data.year2024/assignments)))
  
  (testing "2023"
    (is (s/valid? :roster/entries nlclc-stats.data.year2023/assignments)
        (s/explain-str :roster/entries nlclc-stats.data.year2023/assignments)))

  (testing "2022"
    (is (s/valid? :roster/entries nlclc-stats.data.year2022/assignments)
        (s/explain-str :roster/entries nlclc-stats.data.year2022/assignments))))
