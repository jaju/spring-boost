(ns org.msync.spring-boot-bugger
  (:import [org.springframework.context ApplicationContext]))

(def state (atom {}))
(defn add-application-context [ctx]
  (swap! state assoc :ctx ctx))

(gen-class
  :name
  ^{org.springframework.stereotype.Component ""}
  org.msync.spring_boot_bugger.ClojureComponent
  :state _
  :prefix "-"
  :constructors {[org.springframework.context.ApplicationContext] []}
  :init component-init)

(defn -component-init [^ApplicationContext ctx]
  (add-application-context ctx)
  [[] {}])