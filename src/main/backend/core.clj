(ns backend.core
  (:use ring.adapter.jetty))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(defn -main
  [& args]
  (run-jetty handler {:port 3000
                      :join? false}))
