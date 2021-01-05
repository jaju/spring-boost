(ns org.msync.spring-boot-bugger
  (:import [org.springframework.context ApplicationContext]
           [org.springframework.core.env Environment]))

(gen-class
  :name
  ^{org.springframework.stereotype.Component ""}
  org.msync.spring_boot_bugger.ClojureComponent
  :state state
  :prefix "-"
  :methods [[get-beans-of-type [Class] Object]
            [get-environment [] Environment]]
  :constructors {[org.springframework.context.ApplicationContext] []}
  :init component-init)

(defn -component-init [^ApplicationContext ctx]
  [[] ctx])

(defn -deref
  [this]
  (.state this))

(defn -get-beans-of-type [this clazz]
  (.getBeansOfType (.state this) clazz))

(defn -get-environment [this]
  (.getEnvironment (.state this)))
