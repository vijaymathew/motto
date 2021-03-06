(ns reo.lib.csv
  (:require [reo.util :as u]
            [reo.lib.dt :as dt]
            [reo.tab :as tab])
  (:import [java.io File]
           [java.nio.charset Charset]
           [org.apache.commons.csv CSVFormat
            CSVParser CSVRecord]))

(defn- conv [v t]
  (cond
    (string? t) (dt/dt v t)
    (fn? t) (t v)
    :else
    (case t
      :i (u/safe-parse-int v)
      :f (u/safe-parse-float v)
      :d (u/safe-parse-double v)
      :n (read-string v)
      v)))

(defn- cols [records ^Integer n types]
  (let [indices (range n)
        f (fn [^CSVRecord r]
            (map (fn [^Integer i]
                   (let [v (.get r i)
                         t (get types i)]
                     (if t (conv v t) v)))
                 indices))]
    (map f records)))

(defn fmt []
  CSVFormat/RFC4180)

(defn with-auto-header [p]
  (.withFirstRecordAsHeader p))

(defn with-header [p headers]
  (.withHeader p (into-array String (map name headers))))

(defn with-delim [p c]
  (.withDelimiter p c))

(defn- spread [col-cnt data]
  (loop [data data
         rows (into [] (repeat col-cnt []))]
    (if (seq data)
      (recur (rest data) (u/spread rows (first data)))
      rows)))

(defn- process-types [types coln]
  (if (seq types)
    (loop [types types, rs []]
      (if (seq types)
        (let [t (first types)]
          (recur
           (rest types)
           (conj rs
                 (if (or (string? t) (fn? t))
                   t
                   (keyword t)))))
        rs))
    (into [] (repeat coln nil))))

(defn rd
  ([csv-file ^CSVFormat fmt config]
   (let [config (u/keyword-keys config)
         ^File in (File. csv-file)]
     (let [headers (seq (:headers config))
           charset (Charset/forName (or (:charset config) "UTF-8"))
           ^CSVParser parser (CSVParser/parse in charset fmt)
           records (.getRecords parser)
           hdr (map symbol (or (seq headers) (keys (.getHeaderMap parser))))
           coln (or (:numcols config) (count hdr))
           types (process-types (:types config) coln)]
       [hdr (cols records coln types)])))
  ([csv-file ^CSVFormat fmt]
   (rd csv-file fmt nil))
  ([csv-file]
   (rd csv-file (fmt) nil)))

(defn csv
  ([filename config]
   (let [config (u/keyword-keys config)
         fmt1 CSVFormat/RFC4180
         fmt2 (if (contains? config :auto_header)
                (if (:auto_header config)
                  (with-auto-header fmt1)
                  fmt1)
                (with-auto-header fmt1))
         fmt3 (if-let [delim (:delim config)]
                (with-delim fmt2 delim)
                fmt2)
         [hdr data] (rd filename fmt3 config)
         nhdr (doall (map u/normalized-sym hdr))]
     (tab/mkt nhdr (spread (count hdr) data))))
  ([filename]
   (csv filename nil)))
