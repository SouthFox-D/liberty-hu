(ns backend.page
  (:require [backend.handlers :as handlers]
            [hiccup.page :as page]))

(defn page-template
  [body]
  (page/html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content= "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:meta {:name "referrer" :content "no-referrer"}]
    (page/include-css "/css/app.css")]
   body))

(defn frontend-page
  [_]
  {:status 200
   :content-type {"Content-Type" "text/html; charset=utf-8"}
   :body (page-template
          [:body
           [:div {:id "app"}
            [:div {:class "flex flex-col items-center justify-center h-screen"}
             [:div {:class "loading"}]
             [:p "Loading"]]]
           (page/include-js "/js/main.js")])})
