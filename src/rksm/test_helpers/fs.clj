(ns rksm.test-helpers.fs
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split]]))

(defn file-spec-map->seq
  [base-dir file-spec-map]
  (distinct
   (mapcat
    (fn [[name content]]
      (let [files (reductions io/file (io/file base-dir)
                              (split name #"/|\\"))
            dirs (map (partial hash-map :type :dir :file)
                      (if (string? content) (butlast files) files))]
        (if (map? content)
          (concat dirs (file-spec-map->seq (last files) content))
          (concat dirs [{:file (last files) :content content :type :file}]))))
    file-spec-map)))

(comment
 (file-spec-map->seq
   "test-dir"
   {"foo/bar.txt" "baz"
    "zork/fofofo" {"foo.txt" "oioioi"}}))

(defn create-files
  ([file-spec-map]
   (create-files (io/file ".") file-spec-map))
  ([base-dir file-spec-map]
   (doseq [{:keys [file type content]} (file-spec-map->seq base-dir file-spec-map)]
     (case type
       :dir (-> file .mkdir)
       (spit file content)))))

(defn remove-files
  ([file-spec-map]
   (create-files (io/file ".") file-spec-map))
  ([base-dir file-spec-map]
   (doseq [{:keys [file]} (reverse (file-spec-map->seq base-dir file-spec-map))]
     (-> file .delete))))

(defmacro with-files
  [base-dir file-spec-map & body]
  `(do
     (create-files ~base-dir ~file-spec-map)
     (try
       ~@body
       (finally
         (remove-files ~base-dir ~file-spec-map)))))
