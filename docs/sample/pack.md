# Greedy Packing

This program shows two greedy solutions to the [0/1 Knapsack problem](https://en.wikipedia.org/wiki/Knapsack_problem).

Imagine that we have to pack the following items into a bag that carry a maximum weight of 20u (units - kg or pound).
Each item is encoded as `[item_name, value, weight]`.

```sml
items:[['clock 175 10]
       ['painting 90 9]
       ['radio 20 4]
       ['vase 50 2]
       ['book 10 1]
       ['computer 200 20]]
```

It will be useful to define accessor functions for an item's fields:

```sml
iname:first
ivalue:second
iweight: fn (item) item(2)
```

We can sort the items by either weight, value or the ratio of value to weight.
Then we have to pick the items from the sorted sequence until the bag is full.
Finally we will choose the best packing by comparing the total value of items packed by all three methods.

Here we define the three predicates required for sorting:

```sml
by_wt: fn ([_ _ w1] [_ _ w2]) w1 < w2
by_val: fn ([_ v1 _] [_ v2 _]) v1 > v2
by_rat: fn ([_ v1 w1] [_ v2 w2]) v1/w1 > v2/w2
```

The `pack_bag` function, which we define next, will take a predicate and the maximum allowed weight
as arguments. The `items` are sorted by the predicate and passed to the built-in `pack` function.

The `pack` function works as follows:

It extracts a "weight" from each item in a sequence and moves that item to another sequence until a maximum
weight is achieved. This new sequence along with the rest of the original sequence is returned as a pair.

An example of using `pack`:

```lisp
pack(10 identity [1 5 8 3 4 1 2 7])
; [[1 5 3 1]
;  [2 7]]

pack(10 identity [1 5 8 3 4 2])
; [[1 5 3]]
```

OK, let's define `pack_bag`:

```sml
pack_bag: fn (cmpr maxwt) first(pack(maxwt iweight sort(cmpr, items)))
```

Now we have everything required to start packing!

```lisp
bag_by_wt:pack_bag(by_wt 20)
bag_by_val:pack_bag(by_val 20)
bag_by_rat:pack_bag(by_rat 20)

bag_by_wt
; [[book 10 1]
; [vase 50 2]
; [radio 20 4]
; [painting 90 9]]

bag_by_val
; [[computer 200 20]]

bag_by_rat
; [[vase 50 2]
;  [clock 175 10]
;  [book 10 1]
;  [radio 20 4]]
```

It's just a matter of folding these bags by value to figure out the best method:

```sml
totval: fn (bag) sum(ivalue ~ bag)

w:totval(bag_by_wt)
v:totval(bag_by_val)
r:totval(bag_by_rat)
```
```lisp
[w v r]
; [170 200 255]
```

We may use fold again to automate the task of finding the best method:


```sml
best_of_2: fn ([n1 b1] [n2 b2]) if (b1 > b2 [n1 b1] [n2 b2])
```
```lisp
best_of_2 @ [['by_weight w] ['by_val v] ['by_ratio r]]
; [by_ratio 255]
```

### An optimal solution

The result obtained from `best_of_2` is approximate and not the best.

The best solution can be found by the following algorithm:

1. Compute all subsets of items.
2. Filter out all combinations whose weight exceeds the maximum weight allowed.
3. From the remaining subsets, find the one with the maximum value.

All possible subsets of a set can be obtained by calling the built-in `subsets` function.

We also need a function to find the total weight of a combination:

```sml
totwt: fn (bag) sum(iweight ~ bag)
```

The `best_fits` function defined below will filter out all overweight combinations
from all possible subsets (i.e the [power set](https://en.wikipedia.org/wiki/Power_set) of `items`).
Note that the first subset, which is empty, is ignored by the `filter` operation.

```sml
best_fits: fn (maxwt) (fn(bag) num_lteq(totwt(bag), maxwt)) ! rest(subsets(items))
```

The next function picks the best from the bags returned by `best_fits`.
This is calculated by folding the results by a `maximum-by-value` function.

```sml
max_by_val: fn (a b) if (totval(a) > totval(b) a b)
best_fit: fn (maxwt) max_by_val @ best_fits(maxwt)
```

So what's the optimal combination that gives the maximum value for a bag that can carry 20u?

```lisp
bag:best_fit(20)
bag
; [[clock 175 10]
;  [painting 90 9]
;  [book 10 1]]

totval(bag)
; 275
```

Keep in mind that the optimal algorithm has a time complexity of O(n*2<sup>n</sup>), where `n` is the number of items.
This makes it practical only for very small data-sets.

**Reference** - <a href="https://mitpress.mit.edu/books/introduction-computation-and-programming-using-python-second-edition">Introduction to Computation and Programming Using Python</a>.

[Back](../sample.md)