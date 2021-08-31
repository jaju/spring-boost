(ns org.msync.spring-boot-bugger
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [org.springframework.http.server.reactive ServerHttpRequest]
           [org.springframework.http.server PathContainer]))

(declare handler)

(defn default-handler [& args]
  (println args)
  (stringify-keys {:status (int 501)
                   :headers {:content-type "plain/text"}
                   :body "[spring-boot-bugger] Default: There is no handler installed."}))

(defn -root-handler [^ServerHttpRequest request]
  (let [path (.getPath request)
        context-path (.. path ^PathContainer contextPath ^String value)
        application-path (.. path ^PathContainer pathWithinApplication ^String value)
        http-method (.getMethodValue request)]
    (println (str "path = " path))
    (println (str "method = " http-method))
    (#'handler {:context-path context-path
                :application-path application-path
                :http-method http-method})))

(defn set-handler! [new-handler]
  (alter-var-root #'handler (constantly new-handler)))

(set-handler! default-handler)