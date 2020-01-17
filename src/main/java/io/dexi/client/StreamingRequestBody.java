package io.dexi.client;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.IOException;
import java.io.InputStream;

/**
 * Allows streaming a payload to a request
 */
public class StreamingRequestBody extends RequestBody {
    private final InputStream inputStream;
    private final MediaType contentType;

    public StreamingRequestBody(MediaType contentType, InputStream inputStream) {
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(inputStream);
            sink.writeAll(source);
            sink.flush();
        } finally {
            Util.closeQuietly(source);
        }
    }
}
