(ns stringer.test-barchart
  (:require
    [clojure.java.io :as io]
    [clojure.string  :as str])
  (:import
    [org.jfree.chart ChartFactory ChartUtilities JFreeChart]
    [org.jfree.chart.plot    PlotOrientation]
    [org.jfree.data.category CategoryDataset DefaultCategoryDataset]
    [org.jfree.data.general  PieDataset DefaultPieDataset]))


(defn illegal-arg
  [msg & more]
  (->> (interpose \space more)
    ^String (apply str msg \space)
    (IllegalArgumentException.)
    throw))


(defn make-category-dataset
  "Make category dataset (useful for line chart, bar chart etc.) from given arguments.
  Chart data must be a list of maps. Options include:
  :group-key - the key used for not value, but for group name"
  ^DefaultCategoryDataset [chart-data options]
  (let [^DefaultCategoryDataset dataset (DefaultCategoryDataset.)
        {:keys [group-key]} options
        find-group-name (if group-key
                          #(get % group-key)
                          (let [a (atom 0)]
                            (fn [_] (swap! a inc))))]
    (doseq [each-map chart-data]
      (let [group-name (find-group-name each-map)]
        (doseq [[k v] (seq each-map)]
          (when-not (= group-key k)
            (.addValue dataset ^Number v ^Comparable group-name ^Comparable k)))))
    dataset))


(defn make-category-chart
  "Make line chart from specified arguments. Option keys (with meaning) are below:
  chart-type      :line-chart or :bar-chart
  category-title  category axis label (required)
  value-title     value axis label (required)
  orientation     :horizontal or :vertical (default)
  legend?         true (default) or false
  tooltips?       true (default) or false"
  ^JFreeChart [^CategoryDataset dataset title options]
  (when-not (instance? CategoryDataset dataset)
    (illegal-arg "Expected CategoryDataset instance but found" dataset))
  (let [{:keys [chart-type category-title value-title orientation legend? tooltips?]
         :or {orientation :vertical
              legend? true
              tooltips? true}} options]
    (when-not (string? category-title)
      (illegal-arg "Expected :category-title option to be a string, but found" (pr-str category-title)))
    (when-not (string? value-title)
      (illegal-arg "Expected :value-title option to be a string, but found" (pr-str value-title)))
    (condp = chart-type
      :bar-chart    (ChartFactory/createBarChart title category-title value-title dataset
                      (if (= :horizontal orientation)
                        PlotOrientation/HORIZONTAL
                        PlotOrientation/VERTICAL)
                      legend? tooltips? false)
      :bar-chart-3d (ChartFactory/createBarChart3D title category-title value-title dataset
                      (if (= :horizontal orientation)
                        PlotOrientation/HORIZONTAL
                        PlotOrientation/VERTICAL)
                      legend? tooltips? false)
      :line-chart (ChartFactory/createLineChart title category-title value-title dataset
                    (if (= :horizontal orientation)
                      PlotOrientation/HORIZONTAL
                      PlotOrientation/VERTICAL)
                    legend? tooltips? false)
      (illegal-arg "Expected :chart-type option to be :bar-chart, :bar-chart-3d or :line-chart but found"
        (pr-str chart-type)))))


(defn make-bar-chart
  "Shortcut to make-category-chart (with {:chart-type :bar-chart} in options.)"
  ^JFreeChart [^CategoryDataset dataset title options]
  (make-category-chart dataset title (assoc options :chart-type :bar-chart)))


(defn make-bar-chart-3d
  "Shortcut to make-category-chart (with {:chart-type :bar-chart-3d} in options.)"
  ^JFreeChart [^CategoryDataset dataset title options]
  (make-category-chart dataset title (assoc options :chart-type :bar-chart-3d)))


(defn make-line-chart
  "Shortcut to make-category-chart (with {:chart-type :line-chart} in options.)"
  ^JFreeChart [^CategoryDataset dataset title options]
  (make-category-chart dataset title (assoc options :chart-type :line-chart)))


(defn make-pie-dataset
  "Make pie dataset from specified arguments. Chart data must be a list of maps."
  ^PieDataset [chart-data]
  (let [^DefaultPieDataset dataset (DefaultPieDataset.)]
    (doseq [each-pair chart-data]
      (let [[label value] (if (map? each-pair)
                            (first each-pair)
                            each-pair)]
        (when (nil? label)
          (illegal-arg "Nil encountered for label"))
        (when (nil? value)
          (illegal-arg "Nil encountered for value"))
        (.setValue dataset ^Comparable label ^Number value)))
    dataset))


(defn make-pie-chart
  "Make pie chart from specified arguments. Chart data must be a list of pairs (either a list of single-pair maps,
  or list of two-element vectors.)"
  ^JFreeChart [^PieDataset dataset title options]
  (when-not (instance? PieDataset dataset)
    (illegal-arg "Expected PieDataset instance but found" dataset))
  (let [{:keys [chart-type legend? tooltips?]
         :or {chart-type :pie-chart
              legend? true
              tooltips? true}} options]
    (condp = chart-type
      :pie-chart    (ChartFactory/createPieChart   ^String title ^PieDataset dataset ^boolean legend? ^boolean tooltips? false)
      :pie-chart-3d (ChartFactory/createPieChart3D ^String title ^PieDataset dataset ^boolean legend? ^boolean tooltips? false)
      (illegal-arg "Expected :chart-type option to be :pie-chart or :pie-chart-3d but found"
        (pr-str chart-type)))))


(defn make-pie-chart-3d
  "Short cut to make-pie-chart with {:chart-type :pie-chart-3d} in options."
  [dataset title options]
  (make-pie-chart dataset title (assoc options :chart-type :pie-chart-3d)))


(defn save-chart-as-file
  "Save the specified chart as file. Options can include the following:
  :width   +ve integer (default 640)
  :height  +ve integer (default 480)
  :image-format :png or :jpeg (autodiscovers from filename by default)"
  [^JFreeChart chart file-or-filename options]
  (let [{:keys [width height image-format]
         :or {width 640
              height 480
              image-format (if (string? file-or-filename)
                             (let [lower-name (str/lower-case file-or-filename)]
                               (cond
                                 (.endsWith lower-name ".png")  :png
                                 (.endsWith lower-name ".jpg")  :jpg
                                 (.endsWith lower-name ".jpeg") :jpeg
                                 :otherwise (illegal-arg "Expected PNG or JPEG file type but found"
                                              (pr-str file-or-filename))))
                             (illegal-arg "Expected :image-format option to be :png or :jpeg but found"
                               (pr-str (:image-format options))))}} options
        chart-file (io/as-file file-or-filename)]
    (condp = image-format
      :jpeg (ChartUtilities/saveChartAsJPEG chart-file chart width height)
      :jpg  (ChartUtilities/saveChartAsJPEG chart-file chart width height)
      :png  (ChartUtilities/saveChartAsPNG  chart-file chart width height)
      (illegal-arg "Expected image-format to be :png or :jpeg but found" (pr-str image-format)))))
