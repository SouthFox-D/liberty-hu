(ns frontend.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))
