(ns org.msync.spring-boot-bugger
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [java.util Map]))

(declare handler)

(defn default-handler [& args]
  (println args)
  (stringify-keys {:status (int 501)
                   :headers {:content-type "plain/text"}
                   :body "[spring-boot-bugger] Default: There is no handler installed."}))

(defn -root-handler [^Map request]
  (let []
    (println request)
    (#'handler request)))

(defn set-handler! [new-handler]
  (alter-var-root #'handler (constantly new-handler)))

(set-handler! default-handler)