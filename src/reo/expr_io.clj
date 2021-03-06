(ns reo.expr-io
  (:require [clojure.string :as str]
            [reo.type :as tp]
            [reo.bitvec :as bv]
            [reo.tab :as tab]
            [reo.lib.dt :as dt]))

(declare write)

(defn- writable? [x]
  (not (= x :void)))

(def ^:private max-vec-out 100)

(defn write-vec [v]
  (print "[")
  (when (seq v)
    (loop [v v, i 0]
      (let [x (first v)
            r (rest v)
            fr (first r)]
        (write x)
        (when (and (not (string? x)) (seqable? x) (seq r))
          (when (and (not (string? fr)) (seqable? fr) (seq fr))
            (println)))
        (when (seq r)
          (if (>= i max-vec-out)
            (print " ...")
            (do (print " ")
                (recur r (inc i))))))))
  (print "]"))

(defn- print-bval [b]
  (if b
    (print "1")
    (print "0")))

(defn write-bitvec [bv]
  (bv/for-each print-bval bv)
  (print "b"))

(defn- print-n [n x]
  (loop [n n]
    (when (>= n 0)
      (print x)
      (recur (dec n)))))

(defn- max-row-width [rows]
  (let [cs (fn [row] (map (fn [r] (count (str r))) row))
        css (map cs rows)]
    (apply max (flatten css))))

(defn- write-tab [tab]
  (let [cns (tab/rtcols tab)
        rows (tab/rtdata tab)
        width-cn (inc (max-row-width rows))
        cnsfmt (str "%" width-cn "s")]
    (loop [cns cns, len 0]
      (if (seq cns)
        (let [cn (name (first cns))]
          (print (format cnsfmt cn))
          (recur (rest cns) (+ len (+ width-cn (count cn)))))
        (do (println)
            (print-n len \-)
            (println))))
    (doseq [row rows]
      (doseq [r row]
        (print (format cnsfmt (str r))))
      (println))))

(defn- write-coldict [cd]
  (let [[col-names data] [(tab/tcols cd) (tab/tdata cd)]]
    (loop [cs col-names]
      (when (seq cs)
        (let [k (first cs)
              v (get data k)
              r (rest cs)]
          (write k)
          (print ": ")
          (write v)
          (when (seq r)
            (println))
          (recur r))))))

(defn- write-dict [m]
  (print "[")
  (loop [m m, i 0]
    (when (seq m)
      (if (>= i max-vec-out)
        (print " ...")
        (let [[k v] (first m)]
          (write k)
          (print ":")
          (write v)
          (when (seq (rest m))
            (print " "))
          (recur (rest m) (inc i))))))
  (print "]"))

(defn write-err [e]
  (print (str "ERROR: " (tp/err-data e))))

(defn write-dt [dt]
  (print (str "dt(\"" (dt/sdt dt) "\")")))

(def ^:private t (symbol "1b"))
(def ^:private f (symbol "0b"))

(def ^:private dbl-prec-fmt (atom "%.3f"))

(defn dbl-prec! [i]
  (reset! dbl-prec-fmt (str "%." i "f"))
  i)

(defn- trim-trailing-zeros [^String s]
  (if (str/ends-with? s "0")
    (loop [rs (seq s), i 0, j 0]
      (if (seq rs)
        (if (= (first rs) \0)
          (recur (rest rs) (inc i) (if (zero? j) i j))
          (recur (rest rs) (inc i) 0))
        (if (pos? j)
          (.substring s 0 j)
          s)))
    s))

(defn- print-dbl [v]
  (cond
    (Double/isNaN v) (print "nan")
    (= Double/NEGATIVE_INFINITY v) (print "_inf")
    (= Double/POSITIVE_INFINITY v) (print "inf")
    :else (print (trim-trailing-zeros (format @dbl-prec-fmt v)))))

(defn write [x]
  (when (writable? x)
    (let [v  (cond
               (boolean? x) (if x t f)
               (or (tp/function? x)
                   (fn? x)) '<fn>
               :else x)]
      (cond
        (nil? x) (print 'nul)
        (tab/t? x) (write-coldict x)
        (tab/rt? x) (write-tab x)
        (tp/err? x) (write-err x)
        (string? v) (print v)
        (or (double? v) (float? v)) (print-dbl v)
        (bv/bitvec? x) (write-bitvec x)
        (map? v) (write-dict v)
        (instance? java.util.Calendar v) (write-dt v)
        (seqable? v) (write-vec v)
        :else (print v)))))

(defn writeln [x]
  (when (writable? x)
    (write x)
    (println)))

(defn- match-braces [s opns]
  (loop [ss (seq s), opns opns, clss [0 0 0]]
    (if (seq ss)
      (let [c (first ss)
            [pc bc cc] opns
            [pcc bcc ccc] clss
            [n-opns n-clss]
            (cond
              (= \{ c) [[pc bc (inc cc)] clss]
              (= \} c) [opns [pcc bcc (inc ccc)]]
              (= \( c) [[(inc pc) bc cc] clss]
              (= \) c) [opns [(inc pcc) bcc ccc]]
              (= \[ c) [[pc (inc bc) cc] clss]
              (= \] c) [opns [pcc (inc bcc) ccc]]
              :else [opns clss])]
        (recur (rest ss) n-opns n-clss))
      (map - opns clss))))

(defn readln
  ([brace-counts]
   (if-let [^String s (read-line)]
     (if (str/ends-with? s "\\")
       [:more (.substring s 0 (dec (.length s))) brace-counts]
       (let [c (match-braces s brace-counts)]
         (cond
           (every? #(<= % 0) c) [:done s]
           (some pos? c) [:more (str s " ") c])))
     [:eof nil]))
  ([] (readln [0 0 0])))

(defn read-multiln
  ([stepper]
   (loop [[flag s counts] (readln)
          ss []]
     (case flag
       :more (do (when stepper (stepper))
                 (recur (readln counts)
                        (conj ss s)))
       :done (str/join (conj ss s))
       :eof nil)))
  ([] (read-multiln nil)))
