(ns backend.handlers
  (:require [clojure.string :as str]
            [babashka.http-client :as client]
            [ring.util.codec :refer [url-decode]]
            [cheshire.core :refer [generate-string]])
  (:import [org.jsoup Jsoup]))


(defn wrap-json
  [content]
  (generate-string content))


(defn clean-html
  [docs]
  (-> (.select docs "style[data-emotion-css~=^[a-z0-9]*$]")
      (.remove))
  (vec
   (for [a (.select docs "a")]
     (.attr a "href"
            (-> (.attr a "href")
                (str/replace "https://link.zhihu.com/?target=" "")
                (str/replace "https://zhuanlan.zhihu.com/p/" "#/item/")
                (url-decode))))))


(defn clean-images
  [docs]
  (-> (.select docs "figure > img")
      (.remove))
  (-> (.select docs "figure > noscript")
      (.tagName "div"))
  (vec
   (for [img (.select docs "figure > div > img")]
     (.attr img  "loading" "lazy"))))


(defn render-linkcard
  [docs]
  (doseq [link-card (.select docs "a.LinkCard > span.LinkCard-contents")]
    (.empty link-card)
    (.append link-card (.attr (.parent link-card) "data-text"))))


(defn fetch-hu-post
  [request]
  (let [id       (-> request :path-params :id)
        post-url (str/join ["https://zhuanlan.zhihu.com/p/" id])
        page     (-> (client/get post-url) :body Jsoup/parse)
        title    (.getElementsByClass page "Post-Title")
        docs     (.getElementsByClass page "Post-RichTextContainer")]
    (clean-html docs)
    (clean-images docs)
    (render-linkcard docs)
    (let [content {:content (.toString docs)
                   :title   (.text title)}]
      {:status    200
       :headers   {"Content-Type" "application/json; charset=utf-8"}
       :body      (wrap-json content)})))
