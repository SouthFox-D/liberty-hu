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
   (button 535225379 :question "question")])

(defn about-page []
  [:div
   [:ul
    [:li
     [:a
      {:href "https://git.southfox.me/southfox/liberty-hu"} "Source code"]]]])

(defn history-page []
  (let [histories @(subscribe [:history])]
    [:div {:class "space-y-5"}
     (for [history-item histories]
       ^{:key history-item}
       [:div {:class "p-3 bg-white shadow cursor-pointer drop-shadow-xl hover:bg-slate-200"
              :on-click #(rfe/push-state
                          (:type history-item)
                          {:id (:id history-item)}
                          (:query history-item))}
        [:p {:class "text-2xl"}
         (:title history-item)]
        [:div {:class "mt-3"}
         [:div
          {:dangerouslySetInnerHTML
           {:__html (:detail history-item)}}]]
        [:div {:class "text-sm text-slate-400 text-end"}
         (get-in history-item [:query :cursor])]])]))

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
        end? (-> post :paging :is_end)
        get-more (fn [event params]
                   (.preventDefault event)
                   (dispatch [:get-more params])
                   (dispatch [:set-history
                              {:id (get-in params [:request :id])
                               :type :question
                               :query (get-in params [:request :query])}]))]
    (if (:question loading)
      [:p "Loading..."]
      [:div
       [:h1 {:class "text-2xl"}
        (-> post :question :title)]
       [:div {:class "space-y-5"}
        (for [answer (:answers post)]
          ^{:key answer}
          [:div {:class "p-3 bg-white shadow"}
           [:div {:class "flex items-center mb-3"}
            [:img {:class "h-10 rounded"
                   :src (-> answer :author :avatar_url)}]
            [:a {:class "text-lg ml-3"}
             (-> answer :author :name)]]
           [:div
            {:dangerouslySetInnerHTML
             {:__html (:content answer)}}]
           [:div {:class "text-slate-500 my-2.5"}
            [:p "发布于" (.toLocaleString (js/Date. (* (:created_time answer) 1000)) "zh-CN" {:timezone "UTC"})]
            (when (not= (:created_time answer) (:updated_time answer))
              [:p "编辑于" (.toLocaleString (js/Date. (* (:updated_time answer) 1000)) "zh-CN" {:timezone "UTC"})])]
           [:div {:class "flex gap-4"}
            [:span "🔼" (:voteup_count answer)]
            [:span "🗨️️" (:comment_count answer)]]])]
       (if (:more loading)
         [:p {:class "mt-5"} "loading more..."]
         (if end?
           [:p {:class "mt-5"} "answers end here."]
           [:div {:class "mt-5"}
            [:button
             {:type "button"
              :href (-> post :paging :next)
              :class "btn btn-blue"
              :on-click #(get-more
                          %
                          {:request
                           {:path "hq"
                            :id  (-> post :paging :question_id)
                            :query {:cursor (-> post :paging :cursor)
                                    :session_id (-> post :paging :session_id)}}
                           :db-path [:post :answers]
                           :loading-type :comment})}
             "Load More"]]))])))

(defn nav [{:keys [current-route]}]
  (let [active #(when (= % (-> current-route :data :name)) "> ")]
    [:ul {:class "nav flex flex-col overflow-hidden"}
     [:li
      [:a {:href (rfe/href :frontpage)} (active :frontpage) "Home"]]
     [:li
      [:a {:href (rfe/href :history)} (active :history) "History"]]
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
