package org.msync.spring_boost;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Component
public class Utils {

    public static final IFn requireFn;
    public static final IFn nameFn;
    public static final IFn assocFn;
    public static final IFn dissocFn;
    public static final IFn stringifyKeysFn;
    public static final IFn toRingSpecFn;
    public static final IFn httpHandlerFn;
    public static final IFn websocketHandlerFn;
    public static final IFn setHandlerFn;
    public static final IFn setWebSocketHandlerFn;

    static {
        requireFn = Clojure.var("clojure.core", "require");
        requireFn.invoke(Clojure.read("org.msync.spring-boost"));
        requireFn.invoke(Clojure.read("org.msync.spring-boost.ring-like"));
        requireFn.invoke(Clojure.read("clojure.walk"));

        nameFn = Clojure.var("clojure.core", "name");
        assocFn = Clojure.var("clojure.core", "assoc");
        dissocFn = Clojure.var("clojure.core", "dissoc");
        stringifyKeysFn = Clojure.var("clojure.walk", "stringify-keys");
        toRingSpecFn = Clojure.var("org.msync.spring-boost.ring-like", "to-ring-spec");
        httpHandlerFn = Clojure.var("org.msync.spring-boost", "-http-handler");
        websocketHandlerFn = Clojure.var("org.msync.spring-boost", "-websocket-handler");
        setHandlerFn = Clojure.var("org.msync.spring-boost", "set-handler!");
        setWebSocketHandlerFn = Clojure.var("org.msync.spring-boost", "set-websocket-handler!");
    }

    public static Keyword keyword(String s) {
        return Keyword.intern(s);
    }

    public static String name(Object k) {
        return (String) nameFn.invoke(k);
    }

    // https://stackoverflow.com/questions/46460599/how-to-correctly-read-fluxdatabuffer-and-convert-it-to-a-single-inputstream
    public static InputStream readAsInputStream(Flux<DataBuffer> fdb) throws IOException {
        PipedOutputStream osPipe = new PipedOutputStream();
        PipedInputStream isPipe = new PipedInputStream(osPipe);

        DataBufferUtils.write(fdb, osPipe)
            .subscribe(DataBufferUtils.releaseConsumer());

        return isPipe;
    }
}
