(ns gem_puzzle.core
    (:require [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

#_ (enable-console-print!)
#_ (println "Edits to this text should show up in your developer console.")


(def init-state (shuffle (range 1 16)))

(def world (atom init-state))

(q/defcomponent Root
  [data]
  (html
     [:div {:className "container"}
      (for [i data] (Cell i))]))

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

(defn render [data]
  (q/render (Root data)
            (.getElementById js/document "main-area")))

(add-watch world ::render
           (fn [_ _ _ data] (render data)))

(fw/watch-and-reload :jsload-callback
                     (fn []
                       (swap! world update-in [:tmp-dev] not)))

(defonce *render-puzzle* (render @world))

