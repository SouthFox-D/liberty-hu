(ns frontend.views
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :refer [subscribe]]))


(defn home-page []
  [:div
   :h2 "Home"]
  [:button
   {:type "button"
    :on-click #(rfe/push-state :item {:id 431038004})}
   "Item 431038004"])

(defn about-page []
  [:div
   [:h2 "About"]
   [:ul
    [:li [:a {:href "stub"} "Source code"]]]])

(defn item-page []
  (let [loading @(subscribe [:loading])
        post @(subscribe [:post])]
    (if (:post loading)
      [:p "Loading..."]
      [:div
       [:div {:class "text-2xl"}
        [:h1 (:title post)]]
       [:div
        {:dangerouslySetInnerHTML
         {:__html (:content post)}}]])))

(defn nav [{:keys [current-route]}]
  (let [active #(when (= % (-> current-route :data :name)) "> ")]
    [:ul {:class "nav flex flex-col overflow-hidden"}
     [:li
      [:a {:href (rfe/href :frontpage)} (active :frontpage) "Home"]]
     [:li
      [:a {:href (rfe/href :about)} (active :about) "About"]]
     [:li
      [:a {:href (rfe/href :item {:id 96817849})} (active :item) "Item 96817849"]]]))


(defn current-page []
  (let [current-route @(subscribe [:current-route])]
    [:div {:class "container p-2 mx-auto"}
     [:div {:class "flex flex-row flex-wrap py-4"}
      [:div {:class "w-full sm:w-1/3 md:w-1/4 px-2"}
       [:div {:class "sticky top-0 p-4 bg-slate-300 rounded-xl w-full"}
        [nav {:current-route current-route}]]]
      (when current-route
        [:div {:class "w-full sm:w-2/3 md:w-3/4 pt-1 px-2"}
         [(-> current-route :data :view)]])]]))
