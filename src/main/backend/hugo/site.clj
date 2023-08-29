(ns backend.hugo.site
  (:require [backend.handlers :as handlers]
            [hiccup2.core :as hiccup]))


(defn build-hu-post
  [request]
  (let [content (handlers/fetch-hu-post request {:hugo true})]
    {:status    200
     :headers   {"Content-Type" "text/html; charset=utf-8"}
     :body      (str
                 (hiccup/html {:escape-strings? false}
                   [:html
                    [:head
                     [:meta {:name "referrer" :content "no-referrer"}]]
                    [:body {:style "margin: auto;max-width: 65%;"}
                     [:div
                      [:h1 (:title content)]
                      [:time (:time content)]
                      [:hr]]
                     [:div
                      (:content content)]]]))}))
