(ns gem_puzzle.core
    (:require [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

#_ (enable-console-print!)
#_ (println "Edits to this text should show up in your developer console.")

;; key codes for arrow keys
(def KEYS {37 :left 38 :up 39 :right 40 :down})
(def KEYS-ASWD {65 :left 87 :up 68 :right 83 :down})

(def key-move {:up -4 :down 4 :left -1 :right 1})
;; forbidden moves of numbers by indexes
(def forbidden-moves {3 4 4 3 7 8 8 7 11 12 12 11})

;; randome 16 numbers to build random puzzle. 16-is an empty cell
(def cells-state (atom (shuffle (range 1 17))))

;; index of selected object in cells-state. on start it's 0
(def sel-cell-index (atom 0))

#_ (def game (atom cells-state))

(q/defcomponent Number
  [number]
  (html
     [:div {:className "number"
            :id (str "number-" number)}
            (if (not= 16 number) number)]))

(q/defcomponent Cell
  "one cell of the puzzle"
  [number]
  (html
     [:div {:className "cell"
            :id (str "cell-" number)}
      (Number (get @cells-state (dec number)))]))

(q/defcomponent Root
  []
  (html
     [:div {:className "container"}
      (for [i (range 1 17)] (Cell i))]))

(defn make-selected [index]
  "function to make specific cell selected. depends from index in 'cells-state'"
  (let [x (.getElementById js/document (str "cell-" (inc index)))]
      (set! (.-className x) (str (.-className x) " selected"))))


(defn move-selector [new-index]
  "function with makes new cell selected and old one not. depends from way on keys(left\right etc.)"
  (let [slctd (.getElementById js/document (str "cell-" (inc @sel-cell-index)))]
     (set! (.-className slctd) "cell"))
   (reset! sel-cell-index new-index)
   (make-selected @sel-cell-index))


(defn move-element [new-index]
  "move number elements from one cell to another"
  (let [move-numb (get @cells-state @sel-cell-index)
        replace-numb (get @cells-state new-index)]
    (if (and (= 16 replace-numb) (not= (get forbidden-moves @sel-cell-index) new-index))
      (let [move-what (.getElementById js/document (str "number-" move-numb))
             replace-what (.getElementById js/document (str "number-" replace-numb))]
            (.appendChild (.getElementById js/document (str "cell-" (inc new-index))) move-what)
            (.appendChild (.getElementById js/document (str "cell-" (inc @sel-cell-index))) replace-what)
        (reset! cells-state (replace (hash-map move-numb replace-numb replace-numb move-numb) @cells-state))
        (move-selector new-index)))))


(defn render []
  (q/render (Root)
            (.getElementById js/document "main-area"))
  (.addEventListener js/window "keydown"
    (fn [e]
      (let [key-code (aget e "keyCode")]
      (if-let [key ((merge KEYS KEYS-ASWD) key-code)]
        (let [new-index (+ (key-move key) @sel-cell-index)]
          (if (and (> new-index -1) (< new-index 16))
            (if (contains? KEYS-ASWD key-code)
              (move-element new-index)
              (move-selector new-index))))))))
        (make-selected 0))

#_ (add-watch game ::render
           (fn [_ _ _ data] (render)))

#_ (fw/watch-and-reload :jsload-callback
                     (fn []
                       (swap! game update-in [:tmp-dev] not)))

(defonce *render-puzzle* (render))



