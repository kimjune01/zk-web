(ns zk-web.conf-test
  (:require [clojure.test :refer :all]
            [zk-web.conf :as conf]))

(deftest test-load-conf-env-vars
  (testing "Environment variables should be applied to config"
    (with-redefs [conf/load-conf-file (constantly nil)]
      ;; Mock environment with PORT set but not DEFAULT_NODE
      (with-redefs [conf/getenv (fn [k]
                                  (case k
                                    "PORT" "9090"
                                    "DEFAULT_NODE" nil
                                    "HOME" "/tmp"
                                    nil))]
        (let [loaded-conf (conf/load-conf)]
          (is (= 9090 (:server-port loaded-conf))
              "PORT environment variable should override default server-port")
          (is (= "" (:default-node loaded-conf))
              "default-node should remain empty when DEFAULT_NODE not set")))

      ;; Mock environment with DEFAULT_NODE set but not PORT
      (with-redefs [conf/getenv (fn [k]
                                  (case k
                                    "PORT" nil
                                    "DEFAULT_NODE" "localhost:2181/test"
                                    "HOME" "/tmp"
                                    nil))]
        (let [loaded-conf (conf/load-conf)]
          (is (= 8080 (:server-port loaded-conf))
              "Should use default port when PORT not set")
          (is (= "localhost:2181/test" (:default-node loaded-conf))
              "DEFAULT_NODE environment variable should override default-node")))

      ;; Mock environment with both set
      (with-redefs [conf/getenv (fn [k]
                                  (case k
                                    "PORT" "7070"
                                    "DEFAULT_NODE" "localhost:2181/both"
                                    "HOME" "/tmp"
                                    nil))]
        (let [loaded-conf (conf/load-conf)]
          (is (= 7070 (:server-port loaded-conf))
              "PORT environment variable should be applied")
          (is (= "localhost:2181/both" (:default-node loaded-conf))
              "DEFAULT_NODE environment variable should be applied when both env vars set"))))))
