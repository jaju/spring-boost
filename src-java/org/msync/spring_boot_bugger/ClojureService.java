package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Service
@Configuration
@RestController
@RequestMapping(value = "/internal-dev/clojure", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClojureService {

    private static final Logger logger = Logger.getLogger(ClojureService.class.getName());
    private static IFn serverStartFn;
    private static IFn serverStopFn;
    private static Object server;
    private int port = 7888;

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("nrepl.server"));
        serverStartFn = Clojure.var("nrepl.server", "start-server");
        serverStopFn = Clojure.var("nrepl.server", "stop-server");
    }

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
            logger.info("NREPL server started on port " + port);
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

    @GetMapping(value = "/nrepl-start")
    public ResponseEntity<Object> startNreplHandler() {
        try {
            startNrepl();
            return new ResponseEntity<>(Map.of("status", "started", "port", port), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("status", "error"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/nrepl-stop")
    public ResponseEntity<Object> stopNreplHandler() {
        try {
            stopNrepl();
            return new ResponseEntity<>(Map.of("status", "stopped"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("status", "error"), HttpStatus.BAD_REQUEST);
        }
    }

}