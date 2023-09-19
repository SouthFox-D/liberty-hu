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
 (fn [{:keys [db]} [_ {:keys [new-match id query]}]]
   (let [page (get-in new-match [:data :name])
         set-page (assoc db :current-route new-match)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :frontpage   {:db         set-page}
       ;; -- URL @ "/about" --------------------------------------------------------
       :about       {:db         set-page}

       ;; -- URL @ "/item" --------------------------------------------------------
       :item        {:db         set-page
                     :dispatch   [:get-page {:id id
                                             :query query}]}

       ;; -- URL @ "/item" --------------------------------------------------------
       :question    {:db         set-page
                     :dispatch   [:get-question {:id id
                                                 :query query}]}
       ))))


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

(reg-event-fx
 :get-question
 (fn [{:keys [db]} [_ params]]
   {:fetch      {:method                 :get
                 :url                    (endpoint "hq" (:id params))
                 :params                 (:query params)
                 :mode                   :cors
                 :referrer               :no-referrer
                 :credentials            :omit
                 :timeout                10000
                 :response-content-types {#"application/.*json" :json}
                 :on-success             [:get-question-success]
                 :on-failure             [:get-question-failure]}
    :db         (-> db
                    (assoc-in [:loading :question] true))}))

(reg-event-db
 :get-question-success
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :question] false)
       (assoc-in [:post] body))))

(reg-event-db
 :get-question-failure
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :question] false)
       (assoc :post body))))

(reg-event-fx
 :get-more
 (fn [{:keys [db]} [_ {:keys [request db-path]}]]
   {:fetch      {:method                 :get
                 :url                    (endpoint (:path request) (:id request))
                 :params                 (:query request)
                 :mode                   :cors
                 :referrer               :no-referrer
                 :credentials            :omit
                 :timeout                10000
                 :response-content-types {#"application/.*json" :json}
                 :on-success             [:get-more-success]
                 :on-failure             [:get-question-failure]}
    :db         (-> db
                    (assoc-in [:loading :more] true)
                    (assoc :more-db-path db-path))}))

(reg-event-db
 :get-more-success
 (fn [db [_ {body :body}]]
   (let [db-path (get-in db [:more-db-path])]
     (-> db
         (assoc-in [:loading :more] false)
         (update-in db-path into (:answers body))
         (assoc-in [:post :paging] (:paging body))))))

(reg-event-db
 :initialize-db
 (fn [_ _]
   {:current-route nil}))
