(ns org.msync.spring-boost
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [java.util Map]
           [java.util.logging Logger]
           [org.springframework.web.reactive.socket WebSocketSession HandshakeInfo]
           [org.springframework.core.io.buffer DataBufferFactory]))

(declare ^:private handler)
(declare ^:private websocket-handler)
(defonce ^Logger log (Logger/getLogger (str *ns*)))

(defn -default-handler [& args]
  (.info log (str "Using default handler with args: " args))
  {:status 501
   :headers {:content-type "text/plain"}
   :body "Default: There is no handler installed."})

(defn -default-websocket-handler [session-map]
  (let []))

(defn -websocket-session-handler [^WebSocketSession session]
  (let [^String id (.getId session)
        ^DataBufferFactory factory (.bufferFactory session)
        ^HandshakeInfo handshake-info (.getHandshakeInfo session)]))

(defn -root-handler [^Map request]
  (#'handler request))

(defn -websocket-handler [^WebSocketSession session]
  (#'websocket-handler session))

(defn set-handler! [new-handler]
  (alter-var-root #'handler (constantly new-handler)))

(defn set-websocket-handler! [new-handler]
  (alter-var-root #'websocket-handler (constantly new-handler)))

(defonce _
  (do
    (set-handler! -default-handler)
    (set-websocket-handler! -default-websocket-handler)))