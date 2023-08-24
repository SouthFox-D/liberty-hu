(ns frontend.app
  (:require [reagent.dom.client :as rdom]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :refer [dispatch dispatch-sync clear-subscription-cache!]]
            [frontend.views]
            [frontend.subs]
            [frontend.events]
            [frontend.router]))


(defn on-navigate [new-match]
  (when new-match
    (dispatch [:navigated new-match])))

(defonce root (rdom/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (rdom/render root [frontend.views/current-page {:router frontend.router/router}] ))

(defn init []
  (clear-subscription-cache!)
  (dispatch-sync [:initialize-db])
  (rfe/start!
   frontend.router/router
   on-navigate
   {:use-fragment true})
  (js/console.log "init")
  (start))
