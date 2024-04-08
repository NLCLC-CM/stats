(ns test.nlclc-stats.serde-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [main.nlclc-stats.serde :refer [edn->base64 base64->edn]]))

(deftest edn-b64-converter
  (testing "inverse"
    (is (= {1 1} (base64->edn (edn->base64 {1 1}))))
    (is (= {1 "我"} (base64->edn (edn->base64 {1 "我"}))))
    ))
