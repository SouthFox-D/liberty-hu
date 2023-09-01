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

(defn build-catalog-item
  [catalog-item]
  (str "<li>" "<a href=\""
       (str/join ["#" (.attr catalog-item "id")])
       "\">"
       (.text catalog-item)
       "</a></li>"))

(defn build-catalog
  [docs]
  (let [catalog (.select docs "h2, h3, h4, h5")]
    (apply str (mapv build-catalog-item catalog))))

(defn process-hu-post
  [page hugo]
  (let [post-content  (.getElementsByClass page "Post-RichTextContainer")
        title         (.getElementsByClass page "Post-Title")
        post-time     (.getElementsByClass page "ContentItem-time")]

    (clean-html post-content hugo)
    (clean-images post-content)
    (render-linkcard post-content)

    {:status 200
     :body {:content (.toString post-content)
            :title   (.text title)
            :time    (first (str/split (.text post-time) #"・"))
            :catalog (build-catalog post-content)}}))


(defn fetch-hu-post
  [post-id & {:keys [hugo]}]
  (let [post-url   (str/join ["https://zhuanlan.zhihu.com/p/" post-id])
        page       (-> (client/get post-url) :body Jsoup/parse)
        docs       (.getElementsByClass page "Post-RichTextContainer")]

    (if (empty? docs)
      {:status 404
       :body   {:content "Not Found"
                :title   "Not Found"}}
      (process-hu-post page hugo))))

(defn build-api-hu-post
  [request]
  (let [post-id  (-> request   :path-params :id)
        content  (fetch-hu-post post-id)]
    {:status  (:status content)
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body    (wrap-json (:body content))}))
