(ns org.msync.spring-boot-bugger
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [java.util Map]
           [java.util.logging Logger]))

(declare handler)
(defonce ^Logger log (Logger/getLogger (str *ns*)))

(defn default-handler [& args]
  (.info log (str "Using default handler with args: " args))
  {:status 501
   :headers {:content-type "text/plain"}
   :body "[spring-boot-bugger] Default: There is no handler installed."})

(defn -root-handler [^Map request]
  (#'handler request))

(defn set-handler! [new-handler]
  (alter-var-root #'handler (constantly new-handler)))

(set-handler! default-handler)