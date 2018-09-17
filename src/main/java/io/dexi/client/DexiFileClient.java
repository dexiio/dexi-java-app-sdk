package io.dexi.client;

import com.sun.istack.internal.Nullable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DexiFileClient {

    private static final String UUID_REGEX_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    // Format: FILE:<mimetype>;<size>;<fileId>
    private static final Pattern DEXI_FILE_ID_PATTERN = Pattern.compile("^(FILE:)([^;]*);" +
            "([^;]*);(" + UUID_REGEX_PATTERN + ")$");

    private final RestClient restClient;

    DexiFileClient(Retrofit retrofit) {
        restClient = retrofit.create(RestClient.class);
    }

    /**
     * Determines is string is a dexi file field value. Format is: FILE:<mimetype>;<size>;<fileId>
     */
    public static boolean isFileFieldValue(String fieldValue) {
        Matcher dexiFileIdMatcher = DEXI_FILE_ID_PATTERN.matcher(fieldValue);
        return dexiFileIdMatcher.find();
    }

    /**
     * Gets file stream field from dexi file field value. Format is: FILE:<mimetype>;<size>;<fileId>
     */
    public @Nullable FileHandle getFileFromFieldValue(String value) throws IOException {
        if (!isFileFieldValue(value)) {
            return null;
        }

        Matcher dexiFileIdMatcher = DEXI_FILE_ID_PATTERN.matcher(value);
        String fileId = dexiFileIdMatcher.group(4);

        final Response<ResponseBody> response = restClient.getFile(fileId).execute();

        if (response.isSuccessful()) {
            final ResponseBody responseBody = response.body();
            return new FileHandle(fileId, responseBody.byteStream(), responseBody.contentLength());
        }

        return null;
    }

    public interface RestClient {

        @GET("files/{fileId}")
        Call<ResponseBody> getFile(@Path("fileId") String fileId);
    }

    public static class FileHandle implements AutoCloseable {
        private final String fileId;

        private final InputStream stream;

        private final long size;

        public FileHandle(String fileId, InputStream stream, long size) {
            this.fileId = fileId;
            this.stream = stream;
            this.size = size;
        }

        public String getFileId() {
            return fileId;
        }

        public InputStream getStream() {
            return stream;
        }

        public long getSize() {
            return size;
        }

        public void close() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) { }
            }
        }
    }
}
