(ns frontend.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after trim-v]]
            [superstructor.re-frame.fetch-fx]
            [clojure.string :as str]
            [frontend.db :refer [set-ls-history]]))


(if js/goog.DEBUG
  (def api-url "http://localhost:3000/api")
  (def api-url "./api"))

(defn endpoint
  "Concat params to api-uri by /"
  [& params]
  (str/join "/" (cons api-url params)))

(def set-history-interceptor [(path :history)
                              (after set-ls-history)
                              trim-v])

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [new-match id query]}]]
   (let [page (get-in new-match [:data :name])
         set-page (assoc db :current-route new-match)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :frontpage   {:db         set-page}
       ;; -- URL @ "/history" --------------------------------------------------------
       :history     {:db         set-page}
       ;; -- URL @ "/about" --------------------------------------------------------
       :about       {:db         set-page}
       ;; -- URL @ "/hp" --------------------------------------------------------
       :item        {:db         set-page
                     :dispatch   [:get-page {:id id
                                             :query query}]}
       ;; -- URL @ "/hq" --------------------------------------------------------
       :question    {:db         set-page
                     :dispatch   [:get-question {:id id
                                                 :query query}]}))))

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

(reg-event-fx
 :set-history
 set-history-interceptor
 (fn [{:keys [db]} history]
   (let [history-item (first history)
         select-item (fn [x]
                       (and (= (:id x) (:id history-item))
                        (= (:type x) (:type history-item))))]
     {:db (take 50
                (conj
                 (remove select-item db)
                 (merge (first (filter select-item db)) history-item)))})))

(reg-event-fx
 :get-question-success
 (fn [{:keys [db]} [_ {body :body}]]
   (let [id   (get-in db [:current-route :path-params :id])
         type (get-in db [:current-route :data :name])
         question (get body :question)
         params (if (nil? question)
                  {:id id
                   :type type}
                  {:id id
                   :type type
                   :title (get-in body [:question :title])
                   :detail (get-in body [:question :detail])})]
     {:db (-> db
              (assoc-in [:loading :question] false)
              (assoc-in [:post] body))
      :dispatch [:set-history params]})))

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

(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-history)]
 (fn [{:keys [local-store-history]} _]
   {:db  (assoc {} :history local-store-history)}))
