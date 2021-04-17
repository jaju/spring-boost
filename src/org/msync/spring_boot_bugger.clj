(ns org.msync.spring-boot-bugger
  (:import [org.springframework.context ApplicationContext]))

(defonce state (atom {}))

(gen-class
  :name
  ^{org.springframework.stereotype.Component ""}
  org.msync.spring_boot_bugger.ClojureComponent
  :state _
  :prefix "-"
  :constructors {[org.springframework.context.ApplicationContext] []}
  :init component-init)

(defn -component-init [^ApplicationContext ctx]
  (swap! state assoc :ctx ctx)
  [[] {}])

(defn ^ApplicationContext get-application-context []
  (:ctx @state))

(defn get-beans-of-type [clazz]
  (.getBeansOfType (get-application-context) clazz))

(defn get-environment []
  (.getEnvironment (get-application-context)))
