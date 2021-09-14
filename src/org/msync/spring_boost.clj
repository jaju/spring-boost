(ns org.msync.spring-boost
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [java.util Map]
           [java.util.logging Logger Level]
           [org.springframework.web.reactive.socket WebSocketSession HandshakeInfo WebSocketMessage WebSocketMessage$Type]
           [org.springframework.core.io.buffer DataBufferFactory DataBuffer]
           [org.springframework.util MultiValueMap]
           [java.net URI InetSocketAddress]
           [java.util.function Function Supplier]
           [java.nio.charset StandardCharsets]
           [reactor.core.publisher Flux Mono]))

(defonce ^Logger logger (Logger/getLogger (str *ns*)))

(defn- as-supplier [f]
  (reify Supplier
    (get [_] (if (string? f) f (f)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -default-handler [& args]
  (.info logger (str "Using default handler with args: " args))
  {:status 501
   :headers {:content-type "text/plain"}
   :body "Default: There is no handler installed."})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(declare ^:private handler)

(defn -http-handler [^Map request]
  (#'handler request))

(defn set-handler! [new-handler]
  (alter-var-root #'handler (constantly new-handler)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare ^:private session-handler)

(defn- websocket-session->map [^WebSocketSession session]
  (let [^String id (.getId session)
        ^DataBufferFactory factory (.bufferFactory session)
        ^HandshakeInfo handshake-info (.getHandshakeInfo session)
        ^MultiValueMap headers (.getHeaders handshake-info)
        ^URI uri (.getUri handshake-info)
        ^MultiValueMap cookies (.getCookies handshake-info)
        ^InetSocketAddress remote-address (.getRemoteAddress handshake-info)
        ^Mono principal (.getPrincipal handshake-info)]     ;; TODO - use this info
    {:id id
     :factory factory
     :headers headers
     :uri uri
     :cookies cookies
     :remote-address remote-address}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^"[B" string->bytes [s] (.getBytes s StandardCharsets/UTF_8))
(defn- ^WebSocketMessage wrap-message-handler [message-handler]
  (fn [^WebSocketMessage message]
    (let [typ (.getType message)                            ;; TODO - Is this useful?
          ^DataBuffer db (.getPayload message)
          ^DataBufferFactory factory (.factory db)
          message (.toString db StandardCharsets/UTF_8)]
      (->> message
        (message-handler)
        (string->bytes)
        (.wrap factory)
        (WebSocketMessage. WebSocketMessage$Type/TEXT)))))

(defn -default-session-handler [session]
  (.log logger
    Level/FINE
    ^Supplier (as-supplier #(str "New session: " session)))
  (fn [^String message]
    (str
      "On thread: " (.getName (Thread/currentThread))
     "\nEcho message: " (.toUpperCase message))))

(defn ^Flux -websocket-handler [^WebSocketSession java-session]
  (let [session (websocket-session->map java-session)
        message-handler (#'session-handler session)
        wrapped-message-handler (wrap-message-handler message-handler)
        reified-message-handler (reify Function
                                  (apply [_ in]
                                    (wrapped-message-handler in)))]
   (.map (.receive java-session) reified-message-handler)))

(defn set-websocket-handler! [new-handler]
  (alter-var-root #'session-handler (constantly new-handler)))

(defonce _
  (do
    (set-handler! -default-handler)
    (set-websocket-handler! -default-session-handler)))