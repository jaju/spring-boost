(def version "0.2.0-alpha1")
(def spring-version "5.3.9")
(def spring-boot-version "2.5.4")
(def core-version "1.10.3")

(defproject org.msync/spring-boot-bugger version

  :description "Just add dependency, sprinkle some config, and run Clojure in your Springboot application."

  :url "https://github.com/jaju/spring-boot-bugger"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :scm {:name "git" :url "https://github.com/jaju/spring-boot-bugger"}

  :pom-addition [:developers [:developer [:name "Ravindra R. Jaju"]]]

  :profiles {:provided
             {:dependencies
              [[org.springframework.boot/spring-boot ~spring-boot-version]
               [org.springframework/spring-context ~spring-version]
               [org.springframework/spring-webflux ~spring-version]]}

             :dev
             {:dependencies
              [[org.springframework.boot/spring-boot-starter-webflux ~spring-boot-version]]}}

  :plugins [[org.msync/lein-javadoc "0.4.0-SNAPSHOT"]]

  :source-paths ["src" "src-java"]

  :java-source-paths ["src-java"]

  :javac-options ["-source" "11" "-target" "11"]

  :dependencies [[org.clojure/clojure ~core-version]
                 [nrepl "0.8.3"]]

  :javadoc-opts {:package-names ["org.msync.spring_boot_bugger"]
                 :additional-args ["-windowtitle" "SpringBoot Bugger Javadoc"
                                   "-quiet"
                                   "-link" "https://docs.oracle.com/en/java/javase/11/docs/api/"
                                   "-link" ~(str "https://www.javadoc.io/static/org.clojure/clojure/" core-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-beans/" spring-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-web/" spring-version)
                                   "-link" "https://projectreactor.io/docs/core/release/api/"]}

  :classifiers {:sources {:prep-tasks ^:replace []}
                :javadoc {:prep-tasks ^:replace ["javadoc"]
                          :omit-source true
                          :filespecs ^:replace [{:type :path, :path "javadoc"}]}}

  :repl-options {:init-ns org.msync.spring-boot-bugger}

  :aot [org.msync.spring-boot-bugger.application-context]

  :jar-inclusions [#"spring-boot-bugger-*-sources.jar"]

  :repositories {"snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots"}}

  :deploy-repositories {
                        "releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2" :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots" :creds :gpg}
                        }

  )
