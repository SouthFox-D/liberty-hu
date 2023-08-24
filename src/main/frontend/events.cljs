(ns frontend.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
            [clojure.string :as str]))


(def api-url "http://localhost:3000/hp")

(defn endpoint
  "Concat params to api-uri by /"
  [& params]
  (str/join "/" (cons api-url params)))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [page id]}]]
   (let [set-page (assoc db :active-page page)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       "/" {:db         set-page}))))


(reg-event-db :initialize-db
  (fn [_ _]
    {:set-active-page nil}))

(reg-event-db
 :navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)]
     (assoc db :current-route new-match))))

(def history
  #(dispatch [:set-active-page {:page      (:path %)
                                :id (get-in % [:path-params :id])}]))
