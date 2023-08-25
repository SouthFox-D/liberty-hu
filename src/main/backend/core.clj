(ns backend.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [reitit.ring :as ring]
            [ring.util.response :refer [resource-response content-type]]
            [backend.handlers :as handlers])
  (:gen-class))


(def app
  (ring/ring-handler
   (ring/router
    [["/" (constantly (content-type (resource-response
                        "index.html" {:root "public"}) "text/html; charset=utf-8"))]
     ["/api"
      ["/hp/:id" {:parameters {:path {:id int?}}
                  :get {:handler handlers/fetch-hu-post}}]]])
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))

(defn -main [& args]
  (run-jetty #'app {:port 3000
                    :join? false}))

;; (def server (run-jetty #'app {:port 3000
;;                               :join? false}))
;; (.stop server)
;; (app {:request-method :get
;;       :uri "/api/hp/431038004"
;;       })
