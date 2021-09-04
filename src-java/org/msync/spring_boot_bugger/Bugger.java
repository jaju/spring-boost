package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Namespace;
import clojure.lang.Symbol;
import clojure.lang.Var;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import static org.msync.spring_boot_bugger.Utils.*;

/**
 * The main bean, that installs the core routes for the SpringBootBugger system, that enable
 * 1. The nREPL start/stop end-points
 * 2. The root route handler for this sub-system
 */
public class Bugger {

    private final int nreplPort;
    private final ApplicationContext applicationContext;

    private static final IFn serverStartFn;
    private static final IFn serverStopFn;
    private static Object server;
    private static final Logger logger = Logger.getLogger(Bugger.class.getName());


    static {
        require.invoke(Clojure.read("nrepl.server"));
        serverStartFn = Clojure.var("nrepl.server", "start-server");
        serverStopFn = Clojure.var("nrepl.server", "stop-server");
    }

    private void setupAppInit(String appInitSymbol) {
        logger.info(() -> "Initializing clojure code: " + appInitSymbol);
        Var var = (Var) Clojure.var(appInitSymbol);
        Symbol sym = var.toSymbol();
        String ns = sym.getNamespace();
        require.invoke(Clojure.read(ns));
        var.invoke(applicationContext);
    }

    private synchronized void startNrepl() {
        if (Objects.nonNull(server)) {
            throw new RuntimeException("NREPL service already running.");
        }
        try {
            server = serverStartFn.invoke(Clojure.read(":port"), nreplPort);
            logger.info(() -> "nREPL server started on port = " + nreplPort);
        } catch (Exception e) {
            logger.warning(() -> "Could not start nREPL... " + e.getMessage());
            throw e;
        }
    }

    private synchronized void stopNrepl() {
        if (Objects.isNull(server)) {
            throw new RuntimeException("nREPL server is already stopped.");
        }
        try {
            serverStopFn.invoke(server);
            server = null;
            logger.info("NREPL server stopped.");
        } catch (Exception e) {
            logger.warning(() -> "Could not stop nREPL... " + e.getMessage());
            throw e;
        }
    }

    /**
     * Endpoint to request starting of the nrepl-server
     *
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
     *
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

    public Bugger(int nreplPort, ApplicationContext applicationContext, boolean isNreplStart, String appInitSymbol) {
        this.nreplPort = nreplPort;
        this.applicationContext = applicationContext;
        if (isNreplStart)
            startNrepl();
        if (Objects.nonNull(appInitSymbol)) {
            setupAppInit(appInitSymbol);
        }
    }

}