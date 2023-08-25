(ns backend.handlers
  (:require [clojure.string :as str]
            [babashka.http-client :as client]
            [cheshire.core :refer [generate-string]])
  (:import [org.jsoup Jsoup]))


(defn wrap-json
  [content]
  (generate-string {:content content}))


(defn fetch-hu-post [request]
  (let [id (-> request :path-params :id)
        post-url (str/join ["https://zhuanlan.zhihu.com/p/" id])]

    {:status 200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body (-> (client/get post-url)
               :body
               Jsoup/parse
               (.getElementsByClass "Post-RichTextContainer")
               (.toString)
               (wrap-json))}))
