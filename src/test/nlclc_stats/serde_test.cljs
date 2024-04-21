(ns test.nlclc-stats.serde-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [main.nlclc-stats.serde :refer [edn->base64 base64->edn url->state state->url]]))

(deftest edn-b64-converter
  (testing "inverse"
    (is (= {1 1} (base64->edn (edn->base64 {1 1}))))
    (is (= {1 "我"} (base64->edn (edn->base64 {1 "我"}))))))

(deftest url-state-converter
  (testing "inverse"
    (let [id (comp url->state (partial state->url "http://localhost/"))]
      (doseq [state (list {:tab :history
                           :selected-key nil
                           :sort-dates-asc? false
                           :query {:people #{"Cheuk"}
                                   :songs #{}
                                   :roles #{}
                                   :starting-date nil
                                   :ending-date nil}}
                          {:tab :people
                           :selected-key "Cheuk"
                           :sort-dates-asc? true
                           :query {:people #{}
                                   :songs #{}
                                   :roles #{}
                                   :starting-date "202"
                                   :ending-date "203"}})]
        (is (= state (id state)))))))
