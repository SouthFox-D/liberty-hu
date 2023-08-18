(ns frontend.app
  (:require [reagent.dom.client :as r]))

(defn hello-world []
  [:div
   [:div.hover:bg-gray-100 [:p.text-blue-500 "aaaaaa"]]
   [:li "World!"]])

(defonce root (r/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (r/render root [hello-world] ))

(defn init []
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (start))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))
