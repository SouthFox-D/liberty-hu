(ns backend.core
  (:require [org.httpkit.server :as server]
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
  (server/run-server #'app {:port 3000}))

;; (def web-server (server/run-server #'app {:port 3000}))
;; (web-server)
;; (app {:request-method :get
;;       :uri "/api/hp/431038004"
;;       })
