(ns org.msync.spring-boost.ring-like
  (:require [clojure.string])
  (:import [org.springframework.http.server.reactive ServerHttpRequest SslInfo]
           [org.springframework.web.reactive.function.server ServerRequest]
           [org.springframework.util MultiValueMap]))

;; Reference - https://github.com/ring-clojure/ring/blob/master/SPEC

(defn to-ring-headers [^MultiValueMap spring-headers]
  (persistent!
    (reduce
      (fn [acc [^String k ^List v]]
        (assoc! acc (clojure.string/lower-case k) (clojure.string/join "," v)))
      (transient {})
      spring-headers)))

(defn to-query-string [^ServerHttpRequest request]
  (when-let [^MultiValueMap spring-query-params (.getQueryParams request)]
    (clojure.string/join "&"
      (flatten
        (map
          (fn [[k vs]]
            (map #(str k "=" %) vs))
          spring-query-params)))))

(let [scheme :http
      http-protocol "HTTP/1.1"]
  (defn to-ring-spec [^String uri ^ServerRequest request]
    (let [^ServerHttpRequest http-request (.getRequest (.exchange request))
          local-address (.getLocalAddress http-request)
          server-port (.getPort local-address)
          server-name (.getHostName local-address)
          remote-addr (.getHostName (.getRemoteAddress http-request))
          uri uri
          scheme scheme
          request-method (keyword (clojure.string/lower-case (.getMethodValue http-request)))
          protocol http-protocol
          spring-headers (.getHeaders http-request)
          headers (to-ring-headers spring-headers)]
      (merge
        {:server-port server-port
         :server-name server-name
         :remote-addr remote-addr
         :uri uri
         :scheme scheme
         :request-method request-method
         :protocol protocol
         :headers headers}
        (when-let [^SslInfo ssl-info (.getSslInfo http-request)]
          ;; It's an array, but ring-spec expects just one
          {:ssl-client-cert (aget (.getPeerCertificates ssl-info) 0)})
        (when-let [query-string (to-query-string http-request)]
          ;; Inefficient because spring-framework already parses it, but we re-create an approximation of the query-string
          {:query-string query-string})))))