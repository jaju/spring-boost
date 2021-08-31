(ns org.msync.spring-boot-bugger.application-context
  (:import [org.springframework.core.env Environment]
           [java.util Map]
           [org.springframework.context ApplicationContext]))

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

(defn- -component-init [^ApplicationContext ctx]
  (println "Initializing ClojureComponent...")
  (swap! state assoc :ctx ctx)
  [[] {}])

(defn ^ApplicationContext get-application-context []
  (:ctx @state))

(defn ^String id []
  (.getId (get-application-context)))

(defn ^String application-name []
  (.getApplicationName (get-application-context)))

(defn ^ApplicationContext parent []
  (.getParent (get-application-context)))

(defn ^Environment environment []
  (.getEnvironment (get-application-context)))

(defn ^Map beans-of-type [^Class clazz]
  (.getBeansOfType (get-application-context) clazz))

(defn ^Map beans-with-annotation [^Class annotation]
  (.getBeansWithAnnotation (get-application-context) annotation))