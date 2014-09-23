(ns gem_puzzle.core
    (:require [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

#_ (enable-console-print!)
#_ (println "Edits to this text should show up in your developer console.")

;; key codes for arrow keys
(def KEYS {37 :left 38 :up 39 :right 40 :down})

(def key-move {:up -4 :down 4 :left -1 :right 1})

;; randome 15 numbers to build random puzzle
(def cells-state (shuffle (range 1 16)))

;; index of selected object in cells-state. on start it's 0
(def sel-cell-index (atom 0))

(def game (atom cells-state))

(q/defcomponent Number
  [number]
  (html
     [:div {:className "number"
            :id (str "number-" number)}
            number]))

(q/defcomponent Cell
  "one cell of the puzzle"
  [number]
  (html
     [:div {:className "cell"
            :id (str "cell-" number)}
      (Number number)]))

(q/defcomponent Root
  [data]
  (html
     [:div {:className "container"}
      (for [i data] (Cell i))]))

(defn set-selected[]
  (let [object (first (.getElementsByClassName js/document "cell"))]
       (.className js/object "zzzz")))

(defn make-selected [index]
  "function to make specific cell selected. depends from index in 'cells-state'"
  (let [x (.getElementById js/document (str "cell-" (get cells-state index)))]
      (set! (.-className x) (str (.-className x) " selected"))))

(defn move-selector [way]
  "function with makes new cell selected and old one not. depends from way on keys(left\right etc.)"
  (let [new-way (+ (key-move way) @sel-cell-index)]
        (if (and (> new-way -1) (< new-way 15))
          ((let [slctd (.getElementById js/document (str "cell-" (get cells-state @sel-cell-index)))]
                (set! (.-className slctd) "cell"))
           (reset! sel-cell-index new-way)
           (make-selected @sel-cell-index)))))

(defn render [data]
  (q/render (Root data)
            (.getElementById js/document "main-area"))
  (.addEventListener js/window "keydown"
    (fn [e]
      (if-let [key (KEYS (aget e "keyCode"))]
        (move-selector key))))
        (make-selected 0))

(add-watch game ::render
           (fn [_ _ _ data] (render data)))

(fw/watch-and-reload :jsload-callback
                     (fn []
                       (swap! game update-in [:tmp-dev] not)))

(defonce *render-puzzle* (render @game))



