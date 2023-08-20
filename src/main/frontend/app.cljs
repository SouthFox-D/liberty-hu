(ns frontend.app
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [spec-tools.data-spec :as ds]
            ))


(defn home-page []
  [:div
   :h2 "Home"]
  [:button
   {:type "button"
    :on-click #(rfe/push-state ::item {:id 3})}
   "Item 3"])

(defn about-page []
  [:div
   [:h2 "About"]
   [:ul
    [:li [:a {:href "stub"} "Source code"]]]

   ])

(defn item-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div
     [:h2 "Selected item " id]
     (if (:foo query)
       [:p "Optional foo query param: " (:foo query)])]))

(defonce match (r/atom nil))

(defn current-page []
  [:div {:class "container p-2 mx-auto"}
   [:div {:class "flex flex-row flex-wrap py-4"}
    [:div {:class "w-full sm:w-1/3 md:w-1/4 px-2"}
     [:div {:class "sticky top-0 p-4 bg-slate-300 rounded-xl w-full"}
      [:ul {:class "nav flex flex-col overflow-hidden"}
       [:li [:a {:href (rfe/href ::frontpage)} "Home"]]
       [:li [:a {:href (rfe/href ::about)} "About"]]
       [:li [:a {:href (rfe/href ::item {:id 1})} "Item 1"]]]]]
    (if @match
      (let [view (:view (:data @match))]
        [:div {:class "w-full sm:w-2/3 md:w-3/4 pt-1 px-2"} [view @match]]))]])

(def routes
  [["/"
    {:name ::frontpage
     :view home-page}]
   ["/about"
    {:name ::about
     :view about-page}]
   ["/item/:id"
    {:name ::item
     :view item-page
     :parameters {:path {:id int?}
                  :query {(ds/opt :foo) keyword?}}}]
   ])

(defonce root (rdom/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (rdom/render root [current-page] ))

(defn init []
  (rfe/start!
   (rf/router routes)
   (fn [m] (reset! match m))
   {:use-fragment true})
  (js/console.log "init")
  (start))
