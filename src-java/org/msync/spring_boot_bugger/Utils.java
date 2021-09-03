package org.msync.spring_boot_bugger;

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

    public static final IFn assocFn;
    public static final IFn dissocFn;
    public static final IFn nameFn;
    public static final IFn toRingSpecFn;
    public static final IFn rootHandlerFn;
    public static final IFn stringifyKeysFn;

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("org.msync.spring-boot-bugger"));
        require.invoke(Clojure.read("org.msync.spring-boot-bugger.ring-like"));
        require.invoke(Clojure.read("clojure.walk"));

        assocFn = Clojure.var("clojure.core", "assoc");
        dissocFn = Clojure.var("clojure.core", "dissoc");
        nameFn = Clojure.var("clojure.core", "name");
        toRingSpecFn = Clojure.var("org.msync.spring-boot-bugger.ring-like", "to-ring-spec");
        rootHandlerFn = Clojure.var("org.msync.spring-boot-bugger", "-root-handler");
        stringifyKeysFn = Clojure.var("clojure.walk", "stringify-keys");
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
