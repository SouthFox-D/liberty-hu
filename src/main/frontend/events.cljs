(ns frontend.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [superstructor.re-frame.fetch-fx]
            [clojure.string :as str]))


(def api-url "http://localhost:3000/hp")

(defn endpoint
  "Concat params to api-uri by /"
  [& params]
  (str/join "/" (cons api-url params)))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [new-match id]}]]
   (js/console.log new-match)
   (let [page (get-in new-match [:data :name])
         set-page (assoc db :current-route new-match)]
   (js/console.log page)
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :frontpage {:db         set-page
                   :dispatch [:get-page]}
       ;; -- URL @ "/about" --------------------------------------------------------
       :about     {:db set-page}

       ;; -- URL @ "/item" --------------------------------------------------------
       :item     {:db set-page}
       ))))


(reg-event-fx
 :get-page
 (fn [{:keys [db]} _]
   {:db         (assoc-in db [:loading :tags] true)
    :fetch {:method                 :get
            :url                    "https://api.github.com/orgs/day8"
            :mode                   :cors
            :referrer               :no-referrer
            :credentials            :omit
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [:get-page-success]
            :on-failure             [:initialize-db]
            }}))

(reg-event-db
 :get-page-success
 (fn [db [_ {repos_url :body}]]
   (-> db
       (assoc :repos_url (:id repos_url)) )))

(reg-event-db
 :initialize-db
 (fn [_ _]
   {:current-route nil}))
