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
                     [:meta {:name "referrer" :content "no-referrer"}]
                     [:link {:rel "stylesheet" :href "/css/app.css"}]]
                    [:body
                     [:div {:class "container p-2 mx-auto"}
                      [:div {:class "flex flex-row flex-wrap py-4"}
                       [:div {:class "w-full sm:w-1/3 md:w-1/4 px-2"}
                        [:div {:class "sticky top-0 p-4 bg-slate-300 rounded-xl w-full"}
                         [:div (:catalog content)]]]
                        [:div {:class "w-full sm:w-2/3 md:w-3/4 pt-1 px-2"}
                         [:h1 (:title content)]
                         [:time (:time content)]
                         [:hr]
                         [:div
                          (:content content)]]]]]]))}))
