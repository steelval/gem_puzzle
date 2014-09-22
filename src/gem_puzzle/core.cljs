(ns gem_puzzle.core
    (:require [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

#_ (enable-console-print!)
#_ (println "Edits to this text should show up in your developer console.")


(def world (atom {:text "Hi, Gem Puzzle!"}))

(q/defcomponent Root
  [data]
  (html
     [:h1 (:text data)]))

(defn render [data]
  (q/render (Root data)
            (.getElementById js/document "main-area")))

(add-watch world ::render
           (fn [_ _ _ data] (render data)))

(fw/watch-and-reload :jsload-callback
                     (fn []
                       (swap! world update-in [:tmp-dev] not)))

(defonce *render-puzzle* (render @world))

