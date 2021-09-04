package org.msync.spring_boot_bugger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@ConfigurationProperties(prefix = "clojure-component")
public class BuggerConfiguration {
    private int nreplPort;
    private String rootPath;
    private boolean nreplStart = false;
    private String appInitSymbol;
    @Autowired
    private ApplicationContext applicationContext;

    int getNreplPort() {
        return nreplPort;
    }
    void setNreplPort(int nreplPort) {
        this.nreplPort = nreplPort;
    }

    String getRootPath() {
        return rootPath;
    }
    void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    boolean isNreplStart() {
        return nreplStart;
    }
    void setNreplStart(boolean nreplStart) {
        this.nreplStart = nreplStart;
    }

    public String getAppInitSymbol() {
        return appInitSymbol;
    }

    public void setAppInitSymbol(String appInitSymbol) {
        this.appInitSymbol = appInitSymbol;
    }

    @Bean
    RequestHandler requestHandler() {
        return new RequestHandler(this.rootPath);
    }

    @Bean
    Bugger createBugger() {
        return new Bugger(this.nreplPort, this.applicationContext, this.nreplStart, this.appInitSymbol);
    }

    private String expandedPath(String path) {
        Objects.requireNonNull(path);
        return rootPath + path;
    }

    @Bean
    public RouterFunction<ServerResponse> route(RequestHandler requestHandler, Bugger bugger) {
        return RouterFunctions
            .route(POST(expandedPath("/stop-nrepl")), bugger::stopNreplHandler)
            .andRoute(POST(expandedPath("/start-nrepl")), bugger::startNreplHandler)
            .andRoute(RequestPredicates.path(expandedPath("/**")), requestHandler::clojureHandler);
    }

}
