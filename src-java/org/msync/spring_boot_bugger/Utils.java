package org.msync.spring_boot_bugger;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Utils {
    // https://stackoverflow.com/questions/46460599/how-to-correctly-read-fluxdatabuffer-and-convert-it-to-a-single-inputstream
    public static InputStream readAsInputStream(Flux<DataBuffer> fdb) throws IOException {
        PipedOutputStream osPipe = new PipedOutputStream();
        PipedInputStream isPipe = new PipedInputStream(osPipe);

        DataBufferUtils.write(fdb, osPipe)
            .subscribe(DataBufferUtils.releaseConsumer());

        return isPipe;
    }
}
