;; Tranlsate motto to clojure and eval.
(ns motto.eval-native
  (:require [motto.tokens :as t]
            [motto.parse :as p]
            [motto.compile :as c]
            [motto.util :as u]
            [motto.type :as tp]))

(def ^:private idents (set '(+ - * / = > < >= <=)))

(defn- translate-ident [s]
  (if (= s '/)
    '-div-
    (if (some #{s} idents)
      (symbol (str "-" s "-"))
      s)))

(defn- ex [s]
  (u/ex (str "eval-native: " s)))

(declare ->lisp evaluate-all)

(defn ld [^String file-path eval]
  (let [file-path (if (.endsWith file-path ".mo")
                   file-path
                   (str file-path ".mo"))]
    (when-not (u/file-exists? file-path)
      (c/compile-file (u/normalize-file-path file-path)))
    (let [exprss (c/slurp-o file-path)]
      (loop [exprss exprss, val val]
        (if (seq exprss)
          (let [val (evaluate-all (first exprss) eval)]
            (recur (rest exprss) val))
          val)))))

(def ^:private reserved-names #{'t 'f})

(defn- valid-ident [var]
  (when (some #{var} reserved-names)
    (ex (str "reserved name: " var)))
  var)

(defn all->lisp [exprs eval]
  (map #(->lisp % eval) exprs))

(defn- call-fn [fn args eval]
  (let [fnval (->lisp fn eval)
        eargs (all->lisp args eval)]
    (concat (list fnval) eargs)))

(defn- form->lisp [ident args eval]
  (case ident
    :define `(do (def ~(valid-ident (first args)) ~(->lisp (second args) eval)) ~(first args))
    :call (call-fn (first args) (second args) eval)
    :list (vec (all->lisp (first args) eval))
    :and `(and ~@(all->lisp args eval))
    :or `(or ~@(all->lisp args eval))
    :block `(do ~@(all->lisp (first args) eval))
    :load `(ld ~(first args) eval)
    (call-fn ident args eval)))

(defn- mkfn [fexpr]
  (let [f (:fn fexpr)]
    `(fn ~(into [] (:params f)) ~(->lisp (:body f) eval))))

(defn ->lisp [expr eval]
  (cond
    (= expr :true) true
    (= expr :false) false
    (tp/literal? expr) expr
    (tp/identifier? expr) (translate-ident expr)
    (tp/function? expr) (mkfn expr)
    (seq expr) (form->lisp (first expr) (rest expr) eval)))

(defn evaluate [expr eval]
  (eval (->lisp expr eval)))

(defn evaluate-all [exprs eval]
  (loop [exprs exprs, val nil]
    (if (seq exprs)
      (let [val (evaluate (first exprs) eval)]
        (recur (rest exprs) val))
      val)))