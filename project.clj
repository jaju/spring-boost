(def spring-version "5.3.7")

(defproject org.msync/spring-boot-bugger "0.1.1-SNAPSHOT"

  :description "Just add dependency, sprinkle some config, and run Clojure in your Springboot application."

  :url "https://github.com/jaju/spring-boot-bugger"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :scm {:name "git" :url "https://github.com/jaju/spring-boot-bugger"}

  :pom-addition [:developers [:developer [:name "Ravindra R. Jaju"]]]

  :profiles {:provided
             {:dependencies
              [[org.springframework/spring-context ~spring-version]
               [org.springframework/spring-web ~spring-version]]}}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [nrepl/nrepl "0.8.3"]]

  :source-paths ["src" "src-java"]
  :java-source-paths ["src-java"]

  :repl-options {:init-ns org.msync.spring-boot-bugger}
  :aot [org.msync.spring-boot-bugger]

  :jar-inclusions [#"spring-boot-bugger-*-sources.jar"]

  :deploy-repositories [#_["releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}]
                        #_["snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                      :creds :gpg}]
                        ["releases" :clojars]
                        ["snapshots" :clojars]]

  )
