(ns frontend.db
  (:require [cljs.reader]
            [re-frame.core :refer [reg-cofx]]))

(defn set-ls-history
  [user]
  (.setItem js/localStorage "history" (str user)))

(reg-cofx
 :local-store-history
 (fn [cofx _]
   (assoc cofx :local-store-history
          (into (list)
                (some->> (.getItem js/localStorage "history")
                         (cljs.reader/read-string))))))
