(ns frontend.router
  (:require [reitit.frontend :as rf]
            [re-frame.core :refer [dispatch]]
            [spec-tools.data-spec :as ds]
            [frontend.views]))


(def routes
  [["/"
    [""
     {:name :frontpage
      :view frontend.views/home-page
      :link-text "Home"}]
    ["history"
     {:name :history
      :view frontend.views/history-page
      :link-text "History"}]
    ["about"
     {:name :about
      :view frontend.views/about-page
      :link-text "About"}]
    ["hp/:id"
     {:name :item
      :view frontend.views/item-page
      :link-text "Item"
      :parameters {:path {:id int?}
                   :query {(ds/opt :foo) keyword?}}}]
    ["hq/:id"
     {:name :question
      :view frontend.views/question-page
      :link-text "Question"
      :parameters {:path {:id int?}}}]]])

(def router
  (rf/router
    routes))

(defn on-navigate
  [match _]
  (dispatch [:set-active-page {:new-match  match
                               :id         (get-in match [:path-params :id])
                               :query      (get match :query-params)}]))
