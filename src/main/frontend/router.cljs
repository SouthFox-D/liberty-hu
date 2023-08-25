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
    ["about"
     {:name :about
      :view frontend.views/about-page
      :link-text "About"}]
    ["item/:id"
     {:name :item
      :view frontend.views/item-page
      :link-text "Item"
      :parameters {:path {:id int?}
                   :query {(ds/opt :foo) keyword?}}}]]])

(def router
  (rf/router
    routes))

(def on-navigate
  #(dispatch [:set-active-page {:new-match      %
                                :id        (get-in % [:path-params :id])}]))
