(ns frontend.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

(reg-sub
 :loading
 (fn [db _]
   (:loading db)))

(reg-sub
 :post
 (fn [db _]
   (:post db)))
