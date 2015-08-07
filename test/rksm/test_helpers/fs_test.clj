(ns rksm.test-helpers.fs-test
  (:require [clojure.test :refer :all]
            [rksm.test-helpers.fs :as fs]
            [clojure.java.io :as io]))

(def test-dir (.getCanonicalPath (io/file "./test-dir")))

(defn clean-test-dir
  [test]
  (test)
  (let [f (io/file test-dir)]
    (if (.exists f)
      (doseq [f (reverse (file-seq f))]
        (.delete f)))))

(use-fixtures :each clean-test-dir)

(deftest create-file-system-structure-test
  (fs/with-files test-dir {"test.txt" "this is a test"
                           "foo" {"bar.txt" "more trstsststst"}
                           "zork/foo.txt" "oi oi oi oi"}
    (is (= "this is a test"
           (-> (io/file test-dir "test.txt") slurp)))
    (is (-> (io/file test-dir "foo") .isDirectory))
    (is (= "more trstsststst"
           (-> (io/file test-dir "foo/bar.txt") slurp)))
    (is (-> (io/file test-dir "zork") .isDirectory))
    (is (= "oi oi oi oi"
           (-> (io/file test-dir "zork/foo.txt") slurp))))
  (is (-> (io/file test-dir "test.txt") .exists not)))

(comment
 (let [s (java.io.StringWriter.)] (binding [*test-out* s] (test-ns *ns*) (print (str s))))
 )