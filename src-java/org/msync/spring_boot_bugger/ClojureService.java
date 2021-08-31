package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.CorePublisher;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Bean to control the nrepl-server
 */
@RestController
@RequestMapping(value = "/internal-dev/clojure", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClojureService {

    private static final Logger logger = Logger.getLogger(ClojureService.class.getName());
    private static final IFn serverStartFn;
    private static final IFn serverStopFn;
    private static final IFn rootHandler;
    private static Object server;
    private final int port;

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("nrepl.server"));
        require.invoke(Clojure.read("org.msync.spring-boot-bugger"));
        serverStartFn = Clojure.var("nrepl.server", "start-server");
        serverStopFn = Clojure.var("nrepl.server", "stop-server");
        rootHandler = Clojure.var("org.msync.spring-boot-bugger", "-root-handler");
    }

    /**
     * Constructor
     * @param port - specify which port to use for the nrepl server
     */
    public ClojureService(@Value("${nrepl.port:7888}") int port) {
        this.port = port;
        startNrepl();
    }

    private void startNrepl() {
        if (Objects.nonNull(server)) {
            throw new RuntimeException("NREPL service already running.");
        }
        try {
            server = serverStartFn.invoke(Clojure.read(":port"), Clojure.read(Integer.toString(port)));
            logger.info("[spring-boot-bugger] NREPL server started on port " + port);
        } catch (Exception e) {
            logger.warning("Could not start NREPL...");
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
            logger.warning("Could not stop NREPL...");
            throw e;
        }
    }

    /**
     * Endpoint to request starting of the nrepl-server
     *
     * @return void
     */
    @GetMapping(value = "/nrepl-start")
    public Mono<ResponseEntity> startNreplHandler() {
        try {
            startNrepl();
            return Mono.just(new ResponseEntity<>(Map.of("status", "started", "port", port), HttpStatus.OK));
        } catch (Exception e) {
            return Mono.just(new ResponseEntity<>(Map.of("status", "error"), HttpStatus.CONFLICT));
        }
    }

    /**
     * Endpoint to request stopping of the nrepl-server
     *
     * @return void
     */
    @GetMapping(value = "/nrepl-stop")
    public Mono<ResponseEntity> stopNreplHandler() {
        try {
            stopNrepl();
            return Mono.just(new ResponseEntity<>(Map.of("status", "stopped"), HttpStatus.OK));
        } catch (Exception e) {
            return Mono.just(new ResponseEntity<>(Map.of("status", "error"), HttpStatus.CONFLICT));
        }
    }

    /**
     *
     * @param exchange - Self-evident. Contains all the required information for handling
     *                 a request
     * @return - The response
     */
    @RequestMapping(value = "/")
    public CorePublisher<ResponseEntity> clojureHandler(ServerWebExchange exchange) {
        System.out.println("We got a request.");
        ServerHttpRequest request = exchange.getRequest();
        System.out.println("Request is of type " + request.getMethodValue());
        System.out.println(request.getPath());
        Map<String, Object> response = (Map<String, Object>) rootHandler.invoke(request);
        return updateResponse(response, exchange.getResponse());
    }

    private CorePublisher<ResponseEntity> updateResponse(Map<String, Object> clojureResponse, ServerHttpResponse response) {
        int status = (int) clojureResponse.get("status");
        Map<String, String> headers = (Map<String, String>) clojureResponse.get("headers");
        Object body = clojureResponse.get("body");

        var responseHeaders = response.getHeaders();
        for (var key: headers.keySet()) {
            responseHeaders.add(key, headers.get(key));
        }
        return (Mono.just(new ResponseEntity(body, HttpStatus.valueOf(status))));
    }

}