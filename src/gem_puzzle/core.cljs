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
                 :size size
                 :sqr-size (* size size)})

(defonce world (atom cells-info))

(q/defcomponent Number
  [number sqr-size]
  (html
   [:div {:className "number"}
    (if (not= sqr-size number) number)]))

(q/defcomponent Cell
  "one cell of the puzzle"
  [{:keys [value sqr-size]}]
  (html
   [:div {:className (str "cell" (if (= value sqr-size) " selected" ""))}
    (Number value sqr-size)]))

(q/defcomponent Root
  [{:keys [numbers-state sqr-size]}]
  (q/wrapper
   (html
    [:div {:className "container"}
     (for [cell numbers-state]
       (Cell {:value cell :sqr-size sqr-size}))])
   :onMount
   (fn []
     (.addEventListener
      js/window "keydown"
      (fn [e]
        (when-let [key (KEYS (aget e "keyCode"))]
          (.preventDefault e)
          (perform-move key))
        )))))

(defn perform-move [key]
  (swap! world
         (fn [{:keys [numbers-state sqr-size] :as data}]
           (let [idx (.indexOf (to-array numbers-state) sqr-size)
                 new-idx (+ idx (key-move key))
                 k (get numbers-state idx)
                 v (get numbers-state new-idx)]
             (if (and v)
               (assoc data :numbers-state (replace {k v v k} numbers-state))
               data)))))


(defn request-render [data]
  (.requestAnimationFrame
   js/window
   (fn []
     (js/console.log "render")
     (q/render (Root data) (.getElementById js/document "main-area")))))

(add-watch world ::render
           (fn [_ _ _ new-world]
             (js/console.log (pr-str new-world))
             (request-render new-world)
                                   ))

(defonce *render-puzzle* (request-render @world)))



