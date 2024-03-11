(ns nlclc-stats.data-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [nlclc-stats.data :as data]))

(deftest data-spec-validity
  (testing "2024"
    (is (s/valid? :roster/entries nlclc-stats.data.2024/assignments)
        (s/explain-str :roster/entries nlclc-stats.data.2024/assignments)))
  
  (testing "2023"
    (is (s/valid? :roster/entries nlclc-stats.data.2023/assignments)
        (s/explain-str :roster/entries nlclc-stats.data.2023/assignments))))
