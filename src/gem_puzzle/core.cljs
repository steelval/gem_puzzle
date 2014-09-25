(ns gem_puzzle.core
    (:require [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

; size of a puzzle
(def size 4)

;; key codes for arrow keys
(def KEYS {37 :left 38 :up 39 :right 40 :down})

;; delta of moves depending from way
(def key-move {:up (- 0 size) :down (+ 0 size) :left -1 :right 1})


;; numbers-state - random 16 numbers to build a random puzzle. 16-is an empty cell
;;   (for winning test (def numbers-state (atom (vec (replace {15 16 16 15} (range 1 17))))))
;; selected-cell - index of selected object in numbers-state. on start it's 0
;; size - size of the puzzle
(def cells-info {:numbers-state (shuffle (range 1 (inc (* size size))))
                 :size size})

(defonce world (atom cells-info))

(q/defcomponent Number
  [number size]
  (html
     [:div {:className "number"}
       (if (not= (* size size) number) number)]))

(q/defcomponent Cell
  "one cell of the puzzle"
  [{:keys [value selected size]}]
  (html
     [:div {:className (str "cell" (if selected " selected" ""))}
      (Number value size)]))

(q/defcomponent Root
  [{:keys [numbers-state size]}]
  (html
     [:div {:className "container"}
      (for [cell numbers-state]
        (Cell {:value cell :selected (= cell (* size size)) :size size}))]))


(defn move-selector [new-index]
  "function with makes new cell selected and old one not. depends from way on keys(left\right etc.)"
  (let [slctd (.getElementById js/document (str "cell-" (inc @sel-cell-index)))]
     (set! (.-className slctd) "cell"))
   (reset! sel-cell-index new-index))



(defn move-element [new-index]
  "move number elements from one cell to another"
  (let [move-numb (get @numbers-state @sel-cell-index)
        replace-numb (get @numbers-state new-index)]
    (if (and (= 16 replace-numb) (not= (get forbidden-moves @sel-cell-index) new-index))
        ((reset! numbers-state (replace (hash-map move-numb replace-numb replace-numb move-numb) @numbers-state))
        (move-selector new-index)
         (js/console.log @numbers-state)))))


(defn request-render [data]
    (.requestAnimationFrame js/window
      (fn []
        (js/console.log "render")
        (q/render (Root data) (.getElementById js/document "main-area")))
  ))


(defn start []
  (request-render @world)
  (.addEventListener js/window "keydown"
    (fn [e]
      (let [key-code (aget e "keyCode")
            size (@world :size)
            empiness-index (.indexOf (@world :numbers-state) (* (@world :size) (@world :size)))]
      (if-let [key (KEYS key-code)]
        ;(if (> (- empiness-index (key-move key)) 0)
          (js/console.log empiness-index))))))
  ;)

(add-watch world ::render
           (fn [_ _ _ new-world] (if (= new-world (range 1 17))
                                   (js/console.log "you're winner")
                                   (request-render new-world)
                                   )))

#_ (fw/watch-and-reload :jsload-callback
                     (fn []
                       (swap! game update-in [:tmp-dev] not)))

(defonce *render-puzzle* (start))



