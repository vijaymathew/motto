(ns reo.test.lang
  (:use [clojure.test]
        [reo.test.util]))

(deft arith-test
  ["1 + 2 - 3"        0
   "10 -3*100"        -290
   "(10-3) * 100"     700
   "-12-30"           -42
   "-12--30"          18
   "-(12-30)"         18
   "-(12-30)*100/2"   900
   "3.14 * 12 * .01"  0.3768
   "12.0/0.0"         Double/POSITIVE_INFINITY
   "12.0/0"           Double/POSITIVE_INFINITY
   "10*10+2"          102
   "10*(10+2)"        120
   "10+100/20"        15
   "10+100/20+4"      19
   "10+100+20/4"      115
   "pow(4 2)"         16.0
   "pow(4 [2 1.4 0 0.5, -1, -2.1 5])"
   [16.0 6.964404506368992 1.0 2.0 0.25 0.05440941020600775 1024.0]
   "pow([2 1.4 0 0.5, -1, -2.1 5] 4)"
   [16.0 3.8415999999999992 0.0 0.0625 1.0 19.448100000000004 625.0]
   "pow([2 1.4 0 0.5, -1, -2.1 5] [1 2 3])"
   [2.0 1.9599999999999997 0.0]
   "54%7" 5
   "[216 47 29 28]%2" [0 1 1 0]
   "[54 84 119 19.6] % [7 4 11 4.3]" [5 0 9 2.400000000000002]
   "[29 43, -14, -14]%[3, -5 6, -3]" [2 -2 4 -2]
   "ceil([-1.7 1 1.7])" [-1.0 1.0 2.0]
   "floor([-1.7 1 1.7])" [-2.0 1.0 1.0]
   "ceil(1.5)" 2.0
   "floor(1.5)" 1.0])

(deft cmrp-test
  ["1=1"     true
   "1=1=1b"   :ex
   "1b=(1=1)" true
   "(1=1)=1b" true
   "(1=2)=0b" true
   "0b=0b"     true
   "1=3"     false
   "3=1+2"   true
   "[1 2 3] < [3 4 5]" [true true true]
   "24 <= [24 11 33]" [true false true]
   "100=[99 100 101]" [false true false]
   "bools(bits(100=[99 100 101]) 3)" [false true false]
   "big(2 [1 2 3 4])" [2 2 3 4]
   "sml(2 [1 2 3 4])" [1 2 2 2]
   "big(20 10)"       20
   "sml(20 10)"       10
   "big([20 9 30] 10)" [20 10 30]
   "sml([20 9 30] 10)" [10 9 10]
   "big([20 9 30] [10 20 30])" [20 20 30]
   "sml([20 9 30] [10 20 30])" [10 9 30]
   "[3 8 7] >= [5 8 0]" [false true true]
   "6 > tab([2 3] [7 2 9 3 6 4])" [[false true false] [true false true]]
   "gt([1 2 3] [0 1 2])" true
   "lt([1 2 3] [0 1 2])" false
   "lteq([1 2 3] [1 2 3])" true
   "lt([1 2 3] [1 2 3])" false
   "eq([1 2 3] [1 2 3])" true
   "\"abc\" > \"xyz\"" [false false false]
   "gt(\"abc\" \"xyz\")" false
   "lt(\"abc\" \"xyz\")" true
   "lt(\"abc\" \"hijk\" \"qr\")" true
   "lt(\"abc\" \"hijk\" \"qr\" \"xyz\")" true
   "gt(\"abc\" \"hijk\" \"qr\" \"xyz\")" false
   "eq(\"abc\" [1 2 3 4])" false
   "lteq(\"abc\" [1 2 3 4])" false
   "eq(\"abc\" \"abc\" [1 2 3 4])" false
   "num_eq(100 100)" true
   "num_lt(100 100)" false
   "num_lteq(100 100)" true
   "num_gt(200 100 50)" true
   "num_gteq(200 200 200 200)" true])

(deft logical-test
  ["1<2 & 3<4*100"  true
   "1>2 | 3<4*100"  true
   "a:10<20 & (1=2 | 1 < 2)" 'a
   "a"              true
   "a:(10<10 & (1=2 | 1 < 2))" 'a
   "a"              false
   "a:10"           'a
   "a<10 & (1=2 | 1 < 2)" false
   "a"              10])

(deft lists-test
  ["[1 2 3]"       [1 2 3]
   "[\"Price: \" \"$\" 10+20]" ["Price: " "$" 30]
   "til(5)"        [0 1 2 3 4]
   "2 * til(5)"    [0 2 4 6 8]
   "til(-6)"       [5 4 3 2 1 0]
   "til(2, -5)"    [4 3 2]
   "til(5) + [10 20 30 40 50]" [10 21 32 43 54]
   "dim([10 20 30])" [3]
   "dim(102030)" []
   "dim(dim(102030))" [0]])

(deft dict-test
  ["a:100" 'a
   "d:['a:a 2:3 'b:a*20 'c:[1 2 3] 'd:[1:2 3:4]]" 'd
   "d('a)" 100
   "d(2)" 3
   "d('b)" (* 100 20)
   "d('c)" [1 2 3]
   "d('d)(1)" 2
   "d('d)(3)" 4])

(deft vars-test
  ["fn:100"       :ex
   "if:200"       :ex
   "a:10"        'a
   "a + 2"       12
   "{[a 100, b 4+a] b}"   104
   "a+b"         :ex
   "b:4"         'b
   "a+b"         14
   "a-b"         6
   "a=b"         false
   "b=4"         true
   "(b=4)=(4=b)" true
   "[a b c]:[10 20 a*b]" 'c
   "[a b c]" [10 20 40]
   "xs:[1 2 3]" 'xs
   "[a b c]:xs" 'c
   "a+b+c" 6
   "`a-b:c%d`:1234" (symbol "a-b:c%d")
   "`a-b:c%d`*10" 12340
   "'hello" 'hello
   "'`hello-world**bye`" 'hello-world**bye])

(deft fns-test
  ["(fn (x) x*x)(10)"        100
   "(fn (x y) x*2+y)(10 20)" 40
   "a:fn(x) x*x"             'a
   "a(20)"                   400
   "a:fn(x) fn(y) x+y"       'a
   "b:a(10)"                 'b
   "b(20)"                   30
   "b(b(1))"                 21
   "g:fn(x) fn(y) x + y"     'g
   "g(10)(20)"               30
   "g:fn(x) fn(y) fn (z) x + y + z" 'g
   "g(10)(20)(30)"           60
   "(fn(x) if (x<=0 0 rec(x-1)))(100000)" 0
   "g:fn(x & ys) x * ys" 'g
   "g(10 2)" [20]
   "g(10 2 3 4)" [20 30 40]])

(deft blck-test
  ["{1+2 3+4 5+4}"    9
   "a:10"             'a
   "pyth:fn(x y) {[a x*x b y*y] a+b }" 'pyth
   "pyth(3 4)"        25
   "a"                10
   "{[a 100 b 200] a+b}" 300
   "a"                10
   "{[a 100 b 200] a+b c:3}" 'c
   "c" 3])

(deft op-test
  ["`+`(1 2)"     3
   "`<=`(1 1)"    true])

(deft cond-test
  ["if (1 > 2 200+300)" false
   "if (1 > 2 200+300 \"ok\")" "ok"
   "if (1 < 2 200+300 400)" 500
   "a:100" 'a
   "if (a < 50 1 a < 90 2 a < 100 3 4)" 4
   "a:10" 'a
   "if (a < 50 1 a < 90 2 a < 100 3 4)" 1
   "a:60" 'a
   "if (a < 50 1 a < 90 2 a < 100 3 4)" 2])

(deft burrow-test
  ["a:[[1 2 3] [4 5 6]]" 'a
   "b:[[10 100 100] [1 2 3]]" 'b
   "a*b" [[10 200 300] [4 10 18]]
   "price:[5.2 11.5 3.6 4 8.45]" 'price
   "qty:[2 1 3 6 2]" 'qty
   "costs:price * qty" 'costs
   "costs" [10.4 11.5 10.8 24 16.9]
   "vat:19.6" 'vat
   "price * vat / 100" [1.0192 2.254 0.7056 0.784 1.6562000000000001]
   "forecast:tab([4 6], [150 200 100 80 80 80 300 330 360 400 500 520 100 250 350 380 400 450 50 120 220 300 320 350])" 'forecast
   "actual:tab([4 6] [141 188 111 87 82 74 321 306 352 403 497 507 118 283 397 424 411 409 43  91 187 306 318 363])" 'actual
   "actual - forecast" [[-9 -12 11 7 2 -6] [21 -24 -8 3 -3 -13] [18 33 47 44 11 -41] [-7 -29 -33 6 -2 13]]
   "2 * forecast" [[300 400 200 160 160 160][600 660 720 800 1000 1040][200 500 700 760 800 900][100 240 440 600 640 700]]
   "forecast*2" [[300 400 200 160 160 160][600 660 720 800 1000 1040][200 500 700 760 800 900][100 240 440 600 640 700]]
   "sml(forecast 450)" [[150 200 100 80 80 80][300 330 360 400 450 450][100 250 350 380 400 450][50 120 220 300 320 350]]
   "allbits:fn(bbs) bits~bbs" 'allbits
   "allbools:fn(bbs) (fn(bs) bools(bs 6))~bbs" 'allbools
   "allbools(band(allbits(forecast>350) allbits(actual>forecast)))"
   [[false false false false false false]
    [false false false true false false]
    [false false false true true false]
    [false false false false false false]]])

(deft num-test
  ["-016_ff" -255
   "02_1100110" 102
   "2_147_48_36_47" 2147483647
   "-2147_483_64_8" -2147483648
   "08_99" :ex
   "027_Kona" 411787])

(deft bits-test
  ["bools(band(0101b 0011b))" [false false false true]
   "bools(bor(0101b 0011b))" [false true true true]
   "bools(bxor(0101b 0011b))" [false true true]
   "bools(bvnot(01101b))" [true false false true]
   "a:11001b" 'a
   "bools(band(a 10101b))" [true false false false true]
   "bools(a)" [true true false false true]
   "bools(bvand(a 10101b))" [true false false false true]
   "bools(a)" [true true false false true]
   "bools(bvand(a 10101b 0b))" [true false false false true]
   "bools(a)" [true false false false true]])

(deft short-fn-test
  ["frac:^{[tot float(sum(X1)) d ^X1/tot] d~X1}" 'frac
   "data:[3 1 4]" 'data
   "frac(data)" [0.375 0.125 0.5]
   "percent:^100 * frac(X1)" 'percent
   "percent(data)" [37.5 12.5 50.0]
   "round:^floor(X1 + 0.5)" 'round
   "compute:comp(round percent)" 'compute
   "compute(data)" [38.0 13.0 50.0]
   "show:^zip(X1 compute(X1))" 'show
   "show(data)" [[3 38.0] [1 13.0] [4 50.0]]])

(deft pat-test
  ["ssplit:`clojure.string/split`" 'ssplit
   "ssplit(\"abcXdef\" rx(\"X\"))" ["abc" "def"]])

(deft ex-test
  ["f:fn(x) if (x=0 ex(\"div by 0\") x/10.0)" 'f
   "f(100)" 10.0
   "f(0)" :ex
   "with_ex(^{X1} ^f(0))" "div by 0"])

(deft lazy-test
  ["incs:^lazy(X1 fn()incs(inc(X1)))" 'incs
   "xs:incs(100)" 'xs
   "lift(5 xs)" [100 101 102 103 104]])

(deft ident-keys
  ["[x y]:[10 20]" 'y
   "[x+y x-y x y]" [30 -10 10 20]
   "m:[x:'a y:'b]" 'm
   "m" {10 'a 20 'b}
   "m(x)" 'a
   "m(y)" 'b
   "f:fork(quot `#` rem)" 'f
   "[x y]:f(10 5)" 'y
   "[x y]" [2 0]])

(deft fn-destruct
  ["f:fn([x y]) x + y" 'f
   "f([1 2])" 3
   "g:fn([a:'a b:'b]) a + b" 'g
   "g(['a:4 'b:5])" 9])

(deft scinums
  ["1.0e-15-90" -90.0
   "2.74877906944E11" 2.74877906944E11
   "format(\"%.0f\" 2.74877906944E11)" "274877906944"])

(deft funcs
  ["mnmx:fork(mn `#` mx)" 'mnmx
   "mnmx([10 1 4 20 14 7])" [1 20]
   "rotate:fork(drop `#` take)" 'rotate
   "rotate(3 [1 2 3 4 5])" [4 5 1 2 3]])
