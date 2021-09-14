package org.msync.spring_boost;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import org.springframework.context.ApplicationContext;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.msync.spring_boost.Utils.require;

/**
 * Holds the run-time states
 */
class Boost {

    private final int nreplPort;
    private final ApplicationContext applicationContext;
    private Object server;

    private static final IFn serverStartFn;
    private static final IFn serverStopFn;
    private static final Logger logger = Logger.getLogger(Boost.class.getName());


    static {
        require.invoke(Clojure.read("nrepl.server"));
        serverStartFn = Clojure.var("nrepl.server", "start-server");
        serverStopFn = Clojure.var("nrepl.server", "stop-server");
    }

    private void setInitSymbol(String initSymbol) {
        logger.info(() -> "Initializing clojure code: " + initSymbol);
        Var var = (Var) Clojure.var(initSymbol);
        Symbol sym = var.toSymbol();
        String ns = sym.getNamespace();
        require.invoke(Clojure.read(ns));
        var.invoke(applicationContext);
    }

    protected synchronized void startNrepl() {
        if (Objects.nonNull(server)) {
            throw new RuntimeException("NREPL service already running.");
        }
        try {
            server = serverStartFn.invoke(Clojure.read(":port"), nreplPort);
            logger.info(() -> "nREPL server started on port = " + nreplPort);
        } catch (Exception e) {
            logger.log(Level.SEVERE, () -> "Could not start nREPL... " + e.getMessage());
            throw e;
        }
    }

    protected synchronized void stopNrepl() {
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

    public Boost(ApplicationContext applicationContext, int nreplPort, boolean isNreplStart, String appInitSymbol) {
        this.applicationContext = applicationContext;
        this.nreplPort = nreplPort;
        if (isNreplStart)
            startNrepl();
        if (Objects.nonNull(appInitSymbol)) {
            setInitSymbol(appInitSymbol);
        }
    }

}