(def spring-version "5.3.2")

(defproject org.msync/spring-boot-bugger "0.1.0"

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

  :dependencies [[org.clojure/clojure "1.10.2-rc1"]
                 [nrepl/nrepl "0.8.3"]]
  :java-source-paths ["src-java"]
  :repl-options {:init-ns org.msync.spring-boot-bugger}
  :aot [org.msync.spring-boot-bugger]

  :deploy-repositories [["releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}]
                        ["snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                      :creds :gpg}]])