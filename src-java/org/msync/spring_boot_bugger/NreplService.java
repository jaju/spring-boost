package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@Configuration
public class NreplService {

    private static final Logger logger = Logger.getLogger(NreplService.class.getName());

    public NreplService(@Value("${nrepl.port:7888}") int port) {
        startNrepl(port);
    }

    private void startNrepl(int nreplPort) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("nrepl.server"));
        IFn start = Clojure.var("nrepl.server", "start-server");

        try {
            start.invoke(Clojure.read(":port"), Clojure.read(Integer.toString(nreplPort)));
            logger.info("******** NREPL server started on port " + nreplPort);
        } catch (Exception e) {
            logger.warning("********** Could not start NREPL... **************");
        }
    }

}