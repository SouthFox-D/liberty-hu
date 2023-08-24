(ns frontend.app
  (:require [reagent.dom.client :as rdom]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [spec-tools.data-spec :as ds]
            [re-frame.core :refer [dispatch dispatch-sync reg-event-db clear-subscription-cache! reg-event-fx reg-sub
                                   subscribe ]]))

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
  (let [current-route @(subscribe [::current-route])
        id (get-in current-route [:path-params :id])]
    [:div
     [:h2 "Selected item " id]]))


(def routes
  [["/"
    [""
     {:name :frontpage
      :view home-page
      :link-text "Home"}]
    ["about"
     {:name :about
      :view about-page
      :link-text "About"}]
    ["item/:id"
     {:name :item
      :view item-page
      :link-text "Item"
      :parameters {:path {:id int?}
                   :query {(ds/opt :foo) keyword?}}}]]])

(def router
  (rf/router
    routes
    ))

(reg-event-db :initialize-db
  (fn [_ _]
    {:set-active-page nil}))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [page id]}]]
   (let [set-page (assoc db :active-page page)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       "/" {:db         set-page}))))

(reg-sub ::current-route
        (fn [db]
        (:current-route db)))

(reg-event-db ::navigated
            (fn [db [_ new-match]]
                (let [old-match   (:current-route db)
                    controllers (rfc/apply-controllers (:controllers old-match) new-match)]
                (assoc db :current-route (assoc new-match :controllers controllers)))))


(def history
  #(dispatch [:set-active-page {:page      (:path %)
                                :id (get-in % [:path-params :id])}]))

(defn on-navigate [new-match]
  (when new-match
    (dispatch [::navigated new-match])))

(defn nav [{:keys [current-route]}]
  (js/console.log current-route)
  (let [active #(when (= % (-> current-route :data :name)) "> ")]
    [:ul {:class "nav flex flex-col overflow-hidden"}
     [:li
      [:a {:href (rfe/href :frontpage)} (active :frontpage) "Home"]]
     [:li
      [:a {:href (rfe/href :about)} (active :about) "About"]]
     [:li
      [:a {:href (rfe/href :item {:id 233})} (active :item) "Item 233"]]]))


(defn current-page []
  (let [current-route @(subscribe [::current-route])]
    [:div {:class "container p-2 mx-auto"}
     [:div {:class "flex flex-row flex-wrap py-4"}
      [:div {:class "w-full sm:w-1/3 md:w-1/4 px-2"}
       [:div {:class "sticky top-0 p-4 bg-slate-300 rounded-xl w-full"}
        [nav {:current-route current-route}]]]
      (when current-route
        [:div {:class "w-full sm:w-2/3 md:w-3/4 pt-1 px-2"}
         [(-> current-route :data :view)]])]]))


(defonce root (rdom/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (rdom/render root [current-page {:router router}] ))

(defn init []
  (clear-subscription-cache!)
  (dispatch-sync [:initialize-db])
  (rfe/start!
   router
   on-navigate
   {:use-fragment true})
  (js/console.log "init")
  (start))
