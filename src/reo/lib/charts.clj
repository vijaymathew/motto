(ns reo.lib.charts
  (:require [incanter.core :as ic]
            [incanter.charts :as charts]
            [reo.tab :as tab]
            [reo.util :as u]))

(defn histogram
  ([xs options]
   (let [nbins (get options 'nbins 10)
         density (get options 'density false)
         title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         hg (charts/histogram xs :nbins nbins :density density
                              :title title :x-label x-label
                              :y-label y-label :legend legend
                              :series-label series-label)]
     (when visible?
       (ic/view hg))
     hg))
  ([xs]
   (histogram xs nil)))

(defn qq-plot
  ([xs options]
   (let [data (get options 'data)
         visible? (get options 'visible true)
         qqp (charts/qq-plot xs :data data)]
     (when visible?
       (ic/view qqp))
     qqp))
  ([xs] (qq-plot xs nil)))

(defn scatter-plot
  ([x y options]
   (let [nbins (get options 'nbins 10)
         density (get options 'density false)
         title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         gradient (get options 'gradient false)
         group-by (get options 'groupby)
         sp (charts/scatter-plot x y :nbins nbins :density density
                                 :title title :x-label x-label
                                 :y-label y-label :legend legend
                                 :series-label series-label
                                 :gradient gradient :group-by group-by)]
     (when visible?
       (ic/view sp))
     sp))
  ([x y]
   (scatter-plot x y nil)))

(defn box-plot
  ([x options]
   (let [title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         group-by (get options 'groupby)
         bp (charts/box-plot x :title title :x-label x-label
                             :y-label y-label :legend legend
                             :series-label series-label
                             :group-by group-by)]
     (when visible?
       (ic/view bp))
     bp))
  ([x]
   (box-plot x nil)))

(defn add-box-plot
  ([chart x options]
   (let [series-label (get options 'serieslabel)]
     (charts/add-box-plot chart x :series-label series-label)))
  ([chart x] (charts/add-box-plot chart x)))

(defn xy-plot
  ([x y options]
   (let [data (get options 'data)
         title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         group-by (get options 'groupby)
         points (get options 'points false)
         auto-sort (get options 'autosort true)
         p (charts/xy-plot x y
                           :title title :x-label x-label
                           :y-label y-label :legend legend
                           :series-label series-label
                           :group-by group-by :data data
                           :points points :auto-sort auto-sort)]
     (when visible?
       (ic/view p))
     p))
  ([x y]
   (xy-plot x y nil)))

(defn add-points
  ([chart x y options]
   (let [series-label (get options 'serieslabel)]
     (charts/add-points chart x y :series-label series-label)))
  ([chart x y] (charts/add-points chart x y)))

(defn add-lines
  ([chart x y options]
   (let [series-label (get options 'serieslabel)
         points (get options 'points false)
         auto-sort (get options 'autosort true)]
     (charts/add-lines chart x y :series-label series-label
                       :points points :auto-sort auto-sort)))
  ([chart x y] (charts/add-lines chart x y)))

(defn area-chart
  ([categories values options]
   (let [title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         vertical (get options 'vertical true)
         group-by (get options 'groupby)
         ac (if (get options 'stacked)
              (charts/stacked-area-chart
               categories values
               :title title :x-label x-label
               :y-label y-label :legend legend
               :series-label series-label
               :vertical vertical :group-by group-by)
              (charts/area-chart
               categories values
               :title title :x-label x-label
               :y-label y-label :legend legend
               :series-label series-label
               :vertical vertical :group-by group-by))]
     (when visible?
       (ic/view ac))
     ac))
  ([c v]
   (area-chart c v nil)))

(defn bar-chart
  ([categories values options]
   (let [title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         group-by (get options 'groupby)
         legend (get options 'legend false)
         vertical (get options 'vertical true)
         bc (if (get options 'stacked)
              (charts/stacked-bar-chart
               categories values
               :title title :x-label x-label
               :y-label y-label :group-by group-by
               :legend legend :series-label series-label)
              (charts/bar-chart
               categories values
               :title title :x-label x-label
               :y-label y-label :group-by group-by
               :legend legend :series-label series-label))]
     (when visible?
       (ic/view bc))
     bc))
  ([c v]
   (bar-chart c v nil)))

(defn line-chart
  ([categories values options]
   (let [title (get options 'title)
         x-label (get options 'xlabel)
         y-label (get options 'ylabel)
         legend (get options 'legend false)
         series-label (get options 'serieslabel)
         visible? (get options 'visible true)
         gradient (get options 'gradient false)
         group-by (get options 'groupby)
         lc (charts/line-chart categories values
                               :title title :x-label x-label
                               :y-label y-label :legend legend
                               :series-label series-label
                               :gradient gradient :group-by group-by)]
     (when visible?
       (ic/view lc))
     lc))
  ([c v]
   (line-chart c v nil)))

(defn pie-chart
  ([categories values options]
   (let [title (get options 'title)
         legend (get options 'legend false)
         visible? (get options 'visible true)
         pc (charts/pie-chart categories values
                              :title title :legend legend)]
     (when visible?
       (ic/view pc))
     pc))
  ([c v]
   (pie-chart c v nil)))

(defn view
  ([x options]
   (let [title (get options 'title "Motto View")
         w (get options 'width)
         h (get options 'height)]
     (ic/view (tab/maybe-dset x) :title title :width w :height h))
   nil)
  ([x] (view x nil)))

(defn set-point-size
  ([chart point-sz options]
   (let [series (get options 'series)
         tab (get options 'table)]
     (charts/set-point-size chart point-sz
                            :series series
                            :dataset (when tab
                                       (tab/maybe-dset tab)))))
  ([chart point-sz]
   (set-point-size chart point-sz nil)))

(defn log-axis
  ([options]
   (let [base (get options 'base)
         label (get options 'label)
         int-ticks? (get options 'intticks)
         smallest-value (get options 'smallestvalue)]
     (charts/log-axis :base base :label label
                      :int-ticks? int-ticks?
                      :smallest-value smallest-value)))
  ([] (log-axis nil)))

(defn set-axis [chart dim axis]
  (charts/set-axis chart (keyword dim) axis))

(defn chart-set [chart tag & args]
  (let [f (case tag
            alpha charts/set-alpha
            bgalpha charts/set-background-alpha
            bgdefault charts/set-background-default
            pointsize set-point-size
            title charts/set-title
            xlabel charts/set-x-label
            xrange charts/set-x-range
            ylabel charts/set-y-label
            yrange charts/set-y-range
            axis set-axis
            (u/ex (str "chart-set: invalid tag: " tag)))]
    (apply f chart args)))

(defn- add-categories
  ([chart categories values options]
   (let [group-by (get options 'groupby)
         series-label (get options 'serieslabel)]
     (charts/add-categories chart categories values
                            :group-by group-by
                            :series-label series-label)))
  ([chart categories values] (add-categories chart categories values nil)))

(defn plot-add [chart tag & args]
  (let [f (case tag
            boxplot add-box-plot
            points add-points
            lines add-lines
            categories add-categories
            (u/ex (str "plot-add: invalid tag: " tag)))]
    (apply f chart args)))

(defn plot [tag & args]
  (let [f (case tag
            scatter scatter-plot
            qq qq-plot
            box box-plot
            xy xy-plot
            (u/ex (str "plot: invalid tag: " tag)))]
    (apply f args)))

(defn chart [tag & args]
  (let [f (case tag
            histogram histogram
            area area-chart
            bar bar-chart
            line line-chart
            pie pie-chart
            (u/ex (str "chart: invalid tag: " tag)))]
    (apply f args)))

(defn save
  ([chart filename options]
   (let [w (get options 'width)
         h (get options 'height)]
     (ic/save chart filename :width w :height h)))
  ([chart filename] (save chart filename nil)))
