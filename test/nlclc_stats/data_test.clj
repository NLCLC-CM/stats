(ns nlclc-stats.data-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [nlclc-stats.data :as data]))

(deftest data-spec-validity
  (testing "2024"
    (is (true? (s/valid? :roster/entries nlclc-stats.data.2024/assignments))
        (s/explain-str :roster/entries nlclc-stats.data.2024/assignments))))
