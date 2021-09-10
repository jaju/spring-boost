package org.msync.spring_boost;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@ConfigurationProperties(prefix = "clojure-component")
public class BoosterConfiguration {
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
    Boost createBoost() {
        return new Boost(this.nreplPort, this.applicationContext, this.nreplStart, this.appInitSymbol);
    }

    private String expandedPath(String path) {
        Objects.requireNonNull(path);
        return rootPath + path;
    }

    @Bean
    public RouterFunction<ServerResponse> route(RequestHandler requestHandler, Boost boost) {
        return RouterFunctions
            .route(POST(expandedPath("/stop-nrepl")), boost::stopNreplHandler)
            .andRoute(POST(expandedPath("/start-nrepl")), boost::startNreplHandler)
            .andRoute(RequestPredicates.path(expandedPath("/**")), requestHandler::requestHandler);
    }

    @Bean
    public WebSocketHandler webSocketHandler(RequestHandler requestHandler) {
        return requestHandler.webSocketSessionHandler();
    }

    @Bean
    public HandlerMapping handlerMapping(WebSocketHandler webSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(expandedPath("/ws"), webSocketHandler);
        int order = -1; // before annotated controllers
        return new SimpleUrlHandlerMapping(map, order);
    }

}
