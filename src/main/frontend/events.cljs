(ns frontend.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [superstructor.re-frame.fetch-fx]
            [clojure.string :as str]))



(if js/goog.DEBUG
  (def api-url "http://localhost:3000/api")
  (def api-url "./api"))

(defn endpoint
  "Concat params to api-uri by /"
  [& params]
  (str/join "/" (cons api-url params)))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [new-match id]}]]
   (let [page (get-in new-match [:data :name])
         set-page (assoc db :current-route new-match)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :frontpage   {:db         set-page}
       ;; -- URL @ "/about" --------------------------------------------------------
       :about       {:db         set-page}

       ;; -- URL @ "/item" --------------------------------------------------------
       :item        {:db         set-page
                     :dispatch   [:get-page {:id id}]}))))

(reg-event-fx
 :get-page
 (fn [{:keys [db]} [_ params]]
   {:fetch      {:method                 :get
                 :url                    (endpoint "hp" (:id params))
                 :mode                   :cors
                 :referrer               :no-referrer
                 :credentials            :omit
                 :timeout                10000
                 :response-content-types {#"application/.*json" :json}
                 :on-success             [:get-page-success]
                 :on-failure             [:get-page-failure]}

    :db         (-> db
                    (assoc-in [:loading :post] true))}))

(reg-event-db
 :get-page-success
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :post] false)
       (assoc :post body))))

(reg-event-db
 :get-page-failure
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :post] false)
       (assoc :post body))))

(reg-event-db
 :initialize-db
 (fn [_ _]
   {:current-route nil}))
