(ns backend.handlers
  (:require [clojure.string :as str]
            [babashka.http-client :as client]
            [ring.util.codec :refer [url-decode]]
            [cheshire.core :refer [generate-string parse-string]])
  (:import [org.jsoup Jsoup]))


(defn wrap-json
  [content]
  (generate-string content))

(defn clean-html
  [docs replace-str]
  (-> (.select docs "style[data-emotion-css~=^[a-z0-9]*$]")
      (.remove))
  (doseq [a (.select docs "a")]
    (.attr a "href"
           (-> (.attr a "href")
               (str/replace "https://link.zhihu.com/?target=" "")
               (str/replace "https://zhuanlan.zhihu.com/p/" replace-str)
               (url-decode)))))

(defn clean-images
  [docs]
  (-> (.select docs "figure > img")
      (.remove))
  (-> (.select docs "figure > noscript")
      (.tagName "div"))
  (doseq [img (.select docs "figure > div > img")]
    (.attr img "loading" "lazy")))

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
  [page {:keys [content-type replace-str wrap-fn]}]
  (let [post-content  (.getElementsByClass page "Post-RichTextContainer")
        title         (.getElementsByClass page "Post-Title")
        post-time     (.getElementsByClass page "ContentItem-time")]

    (clean-html post-content replace-str)
    (clean-images post-content)
    (render-linkcard post-content)

    {:status 200
     :headers content-type
     :body (wrap-fn
            {:content (.toString post-content)
             :title   (.text title)
             :time    (first (str/split (.text post-time) #"・"))
             :catalog (build-catalog post-content)})}))

(defn fetch-hu-post
  [post-id params]
  (let [post-url   (str/join ["https://zhuanlan.zhihu.com/p/" post-id])
        page       (-> (client/get post-url) :body Jsoup/parse)
        docs       (.getElementsByClass page "Post-RichTextContainer")]
    (if (empty? docs)
      {:status  404
       :headers (:content-type params)
       :body    (wrap-json {:content "Not Found"
                            :title   "Not Found"})}
      (process-hu-post page params))))

(defn clean-answer-img
  [content]
  (let [docs (Jsoup/parse content)]
    (clean-images docs)
    (.toString docs)))

(defn build-api-hu-post
  [request]
  (let [post-id  (-> request   :path-params :id)]
    (fetch-hu-post post-id
                   {:content-type {"Content-Type" "application/json; charset=utf-8"}
                    :replace-str   "#/hp/"
                    :wrap-fn       wrap-json})))

(defn build-answer
  [m]
  (if (contains? m "target")
    (into {}
          {:author {:avatar_url (get-in m ["target" "author" "avatar_url"] )
                    :url_token  (get-in m ["target" "author" "url_token"]  )
                    :name       (get-in m ["target" "author" "name"])}
           :id            (get-in m ["target" "id"])
           :content       (clean-answer-img (get-in m ["target" "content"]))
           :created_time  (get-in m ["target" "created_time"])
           :updated_time  (get-in m ["target" "updated_time"])
           :comment_count (get-in m ["target" "comment_count"])
           :voteup_count  (get-in m ["target" "voteup_count"])})
    (into {}
          {:author {:avatar_url (get-in m ["author" "avatarUrl"])
                    :url_token  (get-in m ["author" "urlToken"])
                    :name       (get-in m ["author" "name"])}
           :id            (get m "id")
           :content       (clean-answer-img (get m "content"))
           :created_time  (get m "createdTime")
           :updated_time  (get m "updatedTime")
           :comment_count (get m "commentCount")
           :voteup_count  (get m "voteupCount")})))

(defn build-question
  [m]
  (into {}
        {:title          (get m "title")
         :detail         (get m "detail")
         :voteup_count   (get m "voteupCount")
         :visit_count    (get m "visitCount")
         :answer_count   (get m "answerCount")
         :created_time   (get m "created")
         :updated_time   (get m "updatedTime")}))

(defn process-hu-questiion
  [question-json {:keys [content-type wrap-fn]}]
  {:status 200
   :headers content-type
   :body (wrap-fn question-json)})

(defn fetch-hu-answers
  [question-id query]
  (let [question-url (str/join ["https://www.zhihu.com/api/v4/questions/"
                                question-id
                                "/feeds?cursor=" (get query "cursor")
                                "&include=data%5B%2A%5D.is_normal%2Cadmin_closed_comment%2Cis_collapsed%2Cannotation_action%2Cannotation_detail%2Ccollapse_reason%2Cis_sticky%2Ccollapsed_by%2Csuggest_edit%2Ccomment_count%2Ccan_comment%2Ccontent%2Ceditable_content%2Cattachment%2Cvoteup_count%2Ccomment_permission%2Ccreated_time%2Cupdated_time%2Creview_info%2Crelevant_info%2Cquestion%2Cexcerpt%2Cis_labeled%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%2A%5D.mark_infos%5B%2A%5D.url%3Bdata%5B%2A%5D.author.follower_count%2Cvip_info%2Cbadge%5B%2A%5D.topics%3Bdata%5B%2A%5D.settings.table_of_content.enabled"
                                "&limit=5"
                                "&offset=0"
                                "&order=default"
                                "&platform=desktop"
                                "&session_id=" (get query "session_id")])
        question-json (-> (client/get question-url) :body parse-string)]
    question-json))

(defn build-paging-next
  [next-url]
  (let [cursor     (last (re-find #"cursor=(.*?)&" next-url))
        session-id (last (re-find #"session_id=(.*?)(?:&|$)" next-url))]
    {"cursor" cursor
     "session_id" session-id}))

(defn process-api-paging
  [question-id docs]
  (let [next-url       (get docs "next")]
    {:paging (merge
              {"is_end" (get docs "is_end")
               "question_id" question-id}
              (build-paging-next next-url))}))

(defn process-json-paging
  [question-id docs]
  (let [answers (val (first (get docs "answers")))]
    (if (< (count (get answers "ids")) 5)
      {:paging {"is_end"  true}}
      (let [next_url (get answers "next")]
        {:paging (merge
                  {"is_end"  false
                   "question_id" question-id}
                  (build-paging-next next_url))}))))

(defn build-answer-json
  [question-id query]
  (let [answers (fetch-hu-answers question-id query)]
    (merge
     {:answers (mapv build-answer (get answers "data"))}
     (process-api-paging question-id (get answers "paging")))))

(defn fetch-hu-question
  [request params]
  (let [question-id   (-> request :path-params :id)
        query         (-> request :query-params)
        question-url  (str/join ["https://www.zhihu.com/question/"
                                 question-id])
        question-page (-> (client/get question-url) :body Jsoup/parse)
        question-json (-> (.getElementById question-page "js-initialData")
                          .firstChild
                          .toString
                          parse-string)
        question-error? (= (get question-json "subAppName") "errorpage")]
    (if question-error?
      {:status  404
       :headers (:content-type params)
       :body    (wrap-json {:content "Not Found"
                            :title   "Not Found"})}
      (if (empty? query)
        (process-hu-questiion
         (merge
          {:question (build-question (val (first (get-in question-json ["initialState" "entities" "questions"]))))}
          {:answers (mapv build-answer (vals (get-in question-json ["initialState" "entities" "answers"])))}
          (process-json-paging question-id (get-in question-json ["initialState" "question"])))
         params)
        (process-hu-questiion
         (build-answer-json question-id query)
         params)))))

(defn build-api-hu-question
  [request]
  (fetch-hu-question
   request
   {:content-type {"Content-Type" "application/json; charset=utf-8"}
    :wrap-fn       wrap-json}))
