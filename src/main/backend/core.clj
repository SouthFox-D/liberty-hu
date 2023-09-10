(ns backend.core
  (:require [org.httpkit.server :as server]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.coercion.schema]
            [schema.core :as s]
            [reitit.ring.coercion :as rrc]
            [backend.handlers :as handlers]
            [backend.page :as page]
            [backend.hugo.site :as hugo])
  (:gen-class))


(def app
  (ring/ring-handler
   (ring/router
    [["/"        {:parameters {}
                  :get {:handler page/frontend-page}}]
     ["/hp/:id" {:parameters {:path {:id s/Int}}
                 :get {:handler hugo/build-hugo-post}}]
     ["/api"
      ["/hp/:id" {:parameters {:path {:id s/Int}}
                  :get {:handler handlers/build-api-hu-post}}]
      ["/hq/:id" {:parameters {:path {:id s/Int}
                               :query {(s/optional-key :session_id) s/Int
                                       (s/optional-key :cursor) s/Str}}
                  :get {:handler handlers/build-api-hu-question}}]]]
    {:data {:coercion   reitit.coercion.schema/coercion
            :middleware [parameters/parameters-middleware
                         rrc/coerce-request-middleware]}})

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
