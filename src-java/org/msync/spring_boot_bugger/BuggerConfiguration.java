package org.msync.spring_boot_bugger;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "clojure-component")
public class BuggerConfiguration {
    private int nreplPort;
    private String rootPath;
    private boolean nreplStart;

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
}
