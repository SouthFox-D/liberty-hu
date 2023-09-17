(ns frontend.views
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :refer [subscribe dispatch]]))


(defn button
  [id type text]
  [:div
   [:button
    {:type "button"
     :on-click #(rfe/push-state type {:id id})
     :class "btn btn-blue"}
     text " " id]
   [:br]])

(defn home-page []
  [:div
   [:h2 "Try this!"]
   [:p "Post"]
   (button 385214753 :item "Post")
   (button 96817849 :item "Post")
   (button 24425284 :item "Post")
   [:p "questions"]
   (button 432539930 :question "question")
   (button 437735833 :question "question")
   (button 535225379 :question "question")
   ])

(defn about-page []
  [:div
   [:h2 "About"]
   [:ul
    [:li [:a {:href "https://git.southfox.me/southfox/liberty-hu"} "Source code"]]]])

(defn item-page []
  (let [loading @(subscribe [:loading])
        post @(subscribe [:post])]
    (if (:post loading)
      [:p "Loading..."]
      [:div
       [:div {:class "text-2xl"}
        [:h1 (:title post)]
        [:time  (:time  post)]]
       [:div
        {:dangerouslySetInnerHTML
         {:__html (:content post)}}]])))

(defn question-page []
  (let [loading @(subscribe [:loading])
        post @(subscribe [:post])
        end? (-> post :paging :is_end)]
    (if (:question loading)
      [:p "Loading..."]
      [:div
       [:h1 {:class "text-2xl"}
        (-> post :question :title)]
       [:div {:class "space-y-5"}
        (for [ans (:answers post)]
          ^{:key ans}
          [:div {:class "p-3 bg-white shadow rounded-lg"}
           [:div {:class "flex items-center mb-3"}
            [:img {:class "h-10 rounded"
                   :src (-> ans :author :avatar_url)}]
            [:a {:class "text-lg ml-3"}
             (-> ans :author :name)]]
           [:div
            {:dangerouslySetInnerHTML
             {:__html (:content ans)}}]])]
       (if end?
         [:p "answers end here."]
         (if (:comment loading)
           [:p "loading comment..."]
           [:div
            [:button {:type "button"
                      :href (-> post :paging :next)
                      :class "btn btn-blue"
                      :on-click #(rfe/push-state
                                  :question
                                  {:id (-> post :paging :question_id)}
                                  {:cursor (-> post :paging :cursor)
                                   :session_id (-> post :paging :session_id)})}]]))])))

(defn nav [{:keys [current-route]}]
  (let [active #(when (= % (-> current-route :data :name)) "> ")]
    [:ul {:class "nav flex flex-col overflow-hidden"}
     [:li
      [:a {:href (rfe/href :frontpage)} (active :frontpage) "Home"]]
     [:li
      [:a {:href (rfe/href :about)} (active :about) "About"]]]))


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
