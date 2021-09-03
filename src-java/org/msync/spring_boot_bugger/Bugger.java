package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Bean to control the nrepl-server
 */
@Component
public class Bugger {

    private final int nreplPort;
    private final String rootPath;

    private static final IFn serverStartFn;
    private static final IFn serverStopFn;
    private static Object server;
    private static final Logger logger = Logger.getLogger(Bugger.class.getName());


    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("nrepl.server"));
        serverStartFn = Clojure.var("nrepl.server", "start-server");
        serverStopFn = Clojure.var("nrepl.server", "stop-server");
    }

    private void startNrepl() {
        if (Objects.nonNull(server)) {
            throw new RuntimeException("NREPL service already running.");
        }
        try {
            server = serverStartFn.invoke(Clojure.read(":port"), Clojure.read(Integer.toString(nreplPort)));
            logger.info("[spring-boot-bugger] NREPL server started on port " + nreplPort);
        } catch (Exception e) {
            logger.warning("Could not start NREPL... " + e.getMessage());
            throw e;
        }
    }

    private void stopNrepl() {
        if (Objects.isNull(server)) {
            throw new RuntimeException("NREPL service is already down.");
        }
        try {
            serverStopFn.invoke(server);
            server = null;
            logger.info("NREPL server stopped");
        } catch (Exception e) {
            logger.warning("Could not stop NREPL... " + e.getMessage());
            throw e;
        }
    }

    /**
     * Endpoint to request starting of the nrepl-server
     * @param request - The request object
     * @return void
     */
    public Mono<ServerResponse> startNreplHandler(ServerRequest request) {
        try {
            startNrepl();
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "started", "port", nreplPort));
        } catch (Exception e) {
            return ServerResponse
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "error"));
        }
    }

    /**
     * Endpoint to request stopping of the nrepl-server
     * @param request - The request object
     * @return void
     */
    public Mono<ServerResponse> stopNreplHandler(ServerRequest request) {
        try {
            stopNrepl();
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "stopped", "port", nreplPort));
        } catch (Exception e) {
            return ServerResponse
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "error"));
        }
    }

    public Bugger(BuggerConfiguration config) {
        this.nreplPort = config.getNreplPort();
        this.rootPath = config.getRootPath();
        if (config.isNreplStart())
            startNrepl();
    }

    @Bean
    RequestHandler requestHandler() {
        return new RequestHandler(this.rootPath);
    }

    private String path(String path) {
        Objects.requireNonNull(path);
        return rootPath + path;
    }

    @Bean
    public RouterFunction<ServerResponse> route(RequestHandler requestHandler) {
        return RouterFunctions
            .route(POST(path("/stop-nrepl")), this::stopNreplHandler)
            .andRoute(POST(path("/start-nrepl")), this::startNreplHandler)
            .andRoute(RequestPredicates.path(path("/**")), requestHandler::clojureHandler);
    }

}