(ns reo.burrow
  (:require [reo.util :as u]
            [reo.bitvec :as bv]
            [reo.math :as math]))

(declare burrow)

(defn- seq-burrow [opr x y]
  (loop [x x, y y, r []]
    (if (and (seq x) (seq y))
      (recur (rest x) (rest y)
             (conj r (burrow opr (first x) (first y))))
      r)))

(defn- seq-x-burrow
  ([opr x y]
   (loop [x x, r []]
     (if (seq x)
       (recur (rest x) (conj r (burrow opr (first x) y)))
       r)))
  ([opr x]
   (loop [x x, r []]
     (if (seq x)
       (recur (rest x) (conj r (burrow opr (first x))))
       r))))

(defn seq-y-burrow [opr x y]
  (loop [y y, r []]
    (if (seq y)
      (recur (rest y) (conj r (burrow opr x (first y))))
      r)))

(defn burrow
  ([opr x y]
   (cond
     (and (u/atomic? x) (u/atomic? y)) (opr x y)
     (and (seqable? x) (seqable? y)) (seq-burrow opr x y)
     (seqable? x) (seq-x-burrow opr x y)
     :else (seq-y-burrow opr x y)))
  ([opr x]
   (cond
     (u/atomic? x) (opr x)
     :else (seq-x-burrow opr x))))

(defn- not-eq [a b]
  (not (= a b)))

(defn- c< [x y]
  (< (compare x y) 0))

(defn- c> [x y]
  (> (compare x y) 0))

(defn- c>= [x y]
  (let [x (compare x y)]
    (or (> x 0) (= x 0))))

(defn- c<= [x y]
  (let [x (compare x y)]
    (or (< x 0) (= x 0))))

(defn- safe-div [x y]
  (if (zero? y)
    Double/POSITIVE_INFINITY
    (/ x y)))

(defn- safe-mod [x y]
  (if (zero? y)
    Double/POSITIVE_INFINITY
    (mod x y)))

(def add   (partial burrow +))
(def sub   (partial burrow -))
(def mul   (partial burrow *))
(def div   (partial burrow safe-div))
(def residue (partial burrow safe-mod))
(def pow   (partial burrow math/pow))
(def eq    (partial burrow =))
(def neq   (partial burrow not-eq))
(def lt    (partial burrow c<))
(def gt    (partial burrow c>))
(def lteq  (partial burrow c<=))
(def gteq  (partial burrow c>=))
(def big   (partial burrow max))
(def small (partial burrow min))

(def band (partial burrow bv/land))
(def bor (partial burrow bv/lor))
(def bxor (partial burrow bv/xor))
(def band-not (partial burrow bv/and-not))
