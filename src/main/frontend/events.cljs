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
   (let [loading (if (empty? (:query params))
                   :question
                   :comment)]
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
                      (assoc-in [:loading loading] true))})))

(defn merge-question
  [db body]
  (if (empty? (:post db))
    (-> db
        (assoc-in [:post :question] (:question body))
        (assoc-in [:post :answers] (:answers body))
        (assoc-in [:post :paging] (:paging body)))
    (-> db
        (assoc-in [:post :question] (:question body))
        (update-in [:post :answers] into (:answers body))
        (assoc-in [:post :paging] (:paging body)))))

(reg-event-db
 :get-question-success
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :question] false)
       (assoc-in [:loading :comment] false)
       (merge-question body))))

(reg-event-db
 :get-question-failure
 (fn [db [_ {body :body}]]
   (-> db
       (assoc-in [:loading :question] false)
       (assoc :post body))))

(reg-event-db
 :initialize-db
 (fn [_ _]
   {:current-route nil}))
