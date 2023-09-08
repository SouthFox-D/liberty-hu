(ns backend.hugo.site
  (:require [backend.handlers :as handlers]
            [backend.page :as page]
            [hiccup2.core :as hiccup]))


(defn hugo-post-template
  [content]
  (page/page-template
   #(hiccup/html
     {:escape-strings? false}
     [:body
      [:div {:id "app"}
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
           (:content content)]]]]]]
     )))

(defn build-hugo-post
  [request]
  (let [post-id  (-> request   :path-params :id)]
    (handlers/fetch-hu-post
     post-id
     {:content-type {"Content-Type" "text/html; charset=utf-8"}
      :replace-str   "/hp/"
      :wrap-fn       hugo-post-template})))
