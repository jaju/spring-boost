(ns org.msync.spring-boot-bugger
  (:import [org.springframework.context ApplicationContext]
           [org.springframework.core.env Environment]
           [java.util Map]))

(defonce state (atom {}))

(gen-class
  :name
  ^{org.springframework.stereotype.Component ""
    org.springframework.web.bind.annotation.RestController ""
    org.springframework.web.bind.annotation.RequestMapping "/clojure"}
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

(defn ^String get-id []
  (.getId (get-application-context)))

(defn ^String get-application-name []
  (.getApplicationName (get-application-context)))

(defn ^ApplicationContext get-parent []
  (.getParent (get-application-context)))

(defn ^Environment get-environment []
  (.getEnvironment (get-application-context)))

(defn ^Map get-beans-of-type [clazz]
  (.getBeansOfType (get-application-context) clazz))

(defn ^Map get-beans-with-annotation [annotation]
  (.getBeansWithAnnotation (get-application-context) annotation))


