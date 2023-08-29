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
  [docs hugo]
  (let [replace-str (if hugo
                      "/hp/"
                      "#/item/")]
    (-> (.select docs "style[data-emotion-css~=^[a-z0-9]*$]")
        (.remove))
    (doseq [a (.select docs "a")]
      (.attr a "href"
             (-> (.attr a "href")
                 (str/replace "https://link.zhihu.com/?target=" "")
                 (str/replace "https://zhuanlan.zhihu.com/p/" replace-str)
                 (url-decode))))))


(defn clean-images
  [docs]
  (-> (.select docs "figure > img")
      (.remove))
  (-> (.select docs "figure > noscript")
      (.tagName "div"))
  (doseq [img (.select docs "figure > div > img")]
    (.attr img  "loading" "lazy")))


(defn render-linkcard
  [docs]
  (doseq [link-card (.select docs "a.LinkCard > span.LinkCard-contents")]
    (.empty link-card)
    (.append link-card (.attr (.parent link-card) "data-text"))))


(defn fetch-hu-post
  [request & {:keys [hugo]}]
  (let [id       (-> request :path-params :id)
        post-url (str/join ["https://zhuanlan.zhihu.com/p/" id])
        page     (-> (client/get post-url) :body Jsoup/parse)
        title    (.getElementsByClass page "Post-Title")
        post-time     (.getElementsByClass page "ContentItem-time")
        docs     (.getElementsByClass page "Post-RichTextContainer")]
    (clean-html docs hugo)
    (clean-images docs)
    (render-linkcard docs)
    (let [content {:content (.toString docs)
                   :title   (.text title)
                   :time    (first (str/split (.text post-time) #"・"))}]
      (if hugo
        (.toString docs)
        {:status    200
         :headers   {"Content-Type" "application/json; charset=utf-8"}
         :body      (wrap-json content)}))))
