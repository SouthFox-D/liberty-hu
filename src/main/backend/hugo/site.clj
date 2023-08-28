(ns backend.hugo.site
  (:require [backend.handlers :as handlers]
            [hiccup2.core :as hiccup]))


(defn build-hu-post
  [request]

  {:status    200
   :headers   {"Content-Type" "text/html; charset=utf-8"}
   :body      (str (hiccup/html {:escape-strings? false}
                     [:html
                      [:body (handlers/fetch-hu-post request {:hugo true})]]))})
