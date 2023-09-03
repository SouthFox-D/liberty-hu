(ns backend.core
  (:require [org.httpkit.server :as server]
            [reitit.ring :as ring]
            [backend.handlers :as handlers]
            [backend.hugo.site :as hugo])
  (:gen-class))


(def app
  (ring/ring-handler
   (ring/router
    [["/hp/:id" {:parameters {:path {:id int?}}
                 :get {:handler hugo/build-hugo-post}}]
     ["/api"
      ["/hp/:id" {:parameters {:path {:id int?}}
                  :get {:handler handlers/build-api-hu-post}}]
      ["/hq/:id" {:parameters {:path {:id int?}}
                  :get {:handler handlers/build-api-hu-question}}]]])
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
