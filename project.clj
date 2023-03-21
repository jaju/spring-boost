(def version "0.2.0")
(def core-version "1.11.1")
(def spring-version "5.3.25")
(def spring-boot-version "2.7.8")

(defproject org.msync/spring-boost version

  :description "Just add dependency, sprinkle some config, and run Clojure in your Springboot application."

  :url "https://github.com/jaju/spring-boost"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"
            :distribution :repo}

  :scm {:name "git" :url "https://github.com/jaju/spring-boost"}

  :pom-addition [:developers [:developer
                              [:id "jaju"]
                              [:name "Ravindra R. Jaju"]]]

  :profiles {:provided
             {:dependencies
              [[org.springframework.boot/spring-boot ~spring-boot-version]
               [org.springframework/spring-context ~spring-version]
               [org.springframework/spring-webflux ~spring-version]]}

             :dev
             {:dependencies
              [[org.springframework.boot/spring-boot-starter-webflux ~spring-boot-version]
               [org.springframework.boot/spring-boot-configuration-processor ~spring-boot-version]]}}

  :plugins [[org.msync/lein-javadoc "0.4.0-SNAPSHOT"]]

  :source-paths ["src" "src-java"]

  :java-source-paths ["src-java"]

  :javac-options ["-source" "17" "-target" "17"]

  :dependencies [[org.clojure/clojure ~core-version]
                 [nrepl/nrepl "1.0.0"]]

  :javadoc-opts {:package-names ["org.msync.spring_boost"]
                 :additional-args ["-windowtitle" "Spring Boost Javadoc"
                                   "-quiet"
                                   "-link" "https://docs.oracle.com/en/java/javase/17/docs/api/"
                                   "-link" ~(str "https://www.javadoc.io/static/org.clojure/clojure/" core-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-beans/" spring-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-web/" spring-version)
                                   "-link" "https://projectreactor.io/docs/core/release/api/"]}

  :classifiers {:sources {:prep-tasks ^:replace []
                          :source-paths ^:replace ["src"]
                          :java-source-paths ^:replace ["src-java"]
                          :resource-paths ^:replace []}
                :javadoc {:prep-tasks ^:replace ["javadoc"]
                          :omit-source true
                          :filespecs ^:replace [{:type :path, :path "javadoc"}]
                          :source-paths ^:replace ["src"]
                          :java-source-paths ^:replace ["src-java"]
                          :resource-paths ^:replace ["javadoc"]}}

  :repl-options {:init-ns org.msync.spring-boost}

  :aot [org.msync.spring-boost.application-context]

  :jar-inclusions [#"spring-boost-*-sources.jar"]

  :repositories {"snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots"}}

  :deploy-repositories {
                        "releases" #_:clojars {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/" :creds :gpg}
                        "snapshots" #_:clojars {:url "https://oss.sonatype.org/content/repositories/snapshots/" :creds :gpg}
                        }

  )
