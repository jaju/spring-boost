(def spring-version "5.3.2")

(defproject org.msync/spring-boot-bugger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :profiles {:provided
             {:dependencies
              [[org.springframework/spring-context ~spring-version]
               [org.springframework/spring-web ~spring-version]]}}
  :dependencies [[org.clojure/clojure "1.10.2-rc1"]
                 [nrepl/nrepl "0.8.3"]
                 [org.springframework/spring-context ~spring-version]
                 [org.springframework/spring-web ~spring-version]]
  :java-source-paths ["src-java"]
  :repl-options {:init-ns org.msync.spring-boot-bugger}
  :aot [org.msync.spring-boot-bugger]
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
