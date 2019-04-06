(ns motto.tokens
  (:require [motto.str-util :as su]
            [motto.util :as u]))

(def ^:private oprs-kw {\( :openp
                        \) :closep
                        \[ :open-sb
                        \] :close-sb
                        \{ :open-cb
                        \} :close-cb
                        \: :define
                        \+ :plus
                        \- :minus
                        \* :mul
                        \/ :div
                        \= :eq
                        \< :lt
                        \> :gt
                        \# :hash
                        \& :and
                        \| :or
                        \; :semicolon
                        \@ :at
                        \~ :tilde
                        \^ :caret})

(def ^:private oprs (keys oprs-kw))

(defn- ex [s]
  (u/ex (str "tokens: " s)))

(defn- ident-start-char? [ch]
  (or (Character/isAlphabetic (int ch))
      (= ch \_)))

(defn- opr-char? [ch]
  (some #{ch} oprs))

(defn- num-char? [ch]
  (or (Character/isDigit (int ch))
      (= ch \.)))

(defn- str-start-char? [ch]
  (= ch \"))

(def ^:private str-end-char? str-start-char?)

(defn- ident-char? [ch]
  (or (ident-start-char? ch)
      (Character/isDigit (int ch))))

(defn- multichar-token [s predic mk]
  (loop [s s, cs []]
    (if (seq s)
      (let [c (first s)]
        (if (predic c)
          (recur (rest s) (conj cs c))
          [s (mk cs)]))
      [s (mk cs)])))

(defn- ident [cs]
  (symbol (su/implode cs)))

(defn- number [cs]
  (let [need-z? (= (first cs) \.)
        s1 (su/implode cs)
        s2 (if need-z? (str "0" s1) s1)
        v (read-string s2)]
    (when-not (number? v)
      (ex (str "invalid numeric input: " s1)))
    v))

(defn- identifier [s]
  (multichar-token s ident-char? ident))

(defn- operator [s]
  (let [c (first s)]
    (if (= c \^)
      [(rest s) 'recur]
      (let [c2 (second s)
            multopr
            (cond
              (= c \>) (when (= c2 \=)
                         :gteq)
              (= c \<) (cond
                         (= c2 \=) :lteq
                         (= c2 \>) :not-eq)
              (= c \@) (cond
                         (= c2 \~) :fold-incr
                         (= c2 \>) :fold-times))]
        (if multopr
          [(nthrest s 2) multopr]
          [(rest s) (get oprs-kw c)])))))

(defn- num-literal [s]
  (multichar-token s num-char? number))

(defn- str-literal [s]
  (loop [s (rest s), prev-ch \space, cs []]
    (if (seq s)
      (let [ch (first s)]
        (if (str-end-char? ch)
          (if (= prev-ch \\)
            (recur (rest s) ch (conj cs ch))
            [(rest s) (su/implode cs)])
          (recur (rest s) ch (conj cs ch))))
      (ex (str "string not terminated: " (su/implode cs))))))

(defn- escaped-literal [tp s]
  (let [[s cs]
        (loop [s s, cs []]
          (if (seq s)
            (let [c (first s)]
              (cond
                (Character/isWhitespace (int c))
                [s cs]

                (opr-char? c)
                (if (= :char tp)
                  (if (>= (count cs) 2)
                    [s cs]
                    (recur (rest s) (conj cs c)))
                  [s cs])

                :else
                (recur (rest s) (conj cs c))))
            [s cs]))]
    [s (read-string (su/implode cs))]))

(def ^:private char-literal (partial escaped-literal :char))
(def ^:private sym-literal (partial escaped-literal :sym))

(defn- tokenizer [ch]
  (cond
    (ident-start-char? ch) identifier
    (opr-char? ch) operator
    (num-char? ch) num-literal
    (str-start-char? ch) str-literal
    (= ch \\) char-literal
    (= ch \') sym-literal
    :else (ex (str "invalid character in input: " ch))))

(defn- normalize [tokens]
  (or (seq tokens)
      [:void]))

(defn tokens [s]
  (loop [s s, ts []]
    (if (seq s)
      (let [c (first s)]
        (if (Character/isWhitespace (int c))
          (recur (rest s) ts)
          (let [tf (tokenizer c)
                [s t] (tf s)]
            (recur s (conj ts t)))))
      (normalize ts))))
