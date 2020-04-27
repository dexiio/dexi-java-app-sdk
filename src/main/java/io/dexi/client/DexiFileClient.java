package io.dexi.client;

import okhttp3.ResponseBody;
import org.apache.commons.lang.StringUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DexiFileClient {

    private static final Logger log = LoggerFactory.getLogger(DexiFileClient.class.getName());

    // Format: FILE:<mimetype>;<size>;<fileId>
    private static final Pattern DEXI_FILE_ID_PATTERN = Pattern.compile("^(FILE:)([^;]*);" +
            "([^;]*);(.+)$");

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
    public FileHandle getFileFromFieldValue(String value) throws IOException {
        if (!isFileFieldValue(value)) {
            return null;
        }

        String fileSizeString;
        String fileId;
        try {
            Matcher dexiFileIdMatcher = DEXI_FILE_ID_PATTERN.matcher(value);
            if (!dexiFileIdMatcher.matches()) {
                log.debug("Invalid file pointer: {}", value);
                return null;
            }

            fileSizeString = dexiFileIdMatcher.group(3);
            fileId = dexiFileIdMatcher.group(4);
        } catch (IllegalStateException e) {
            log.warn("Failed to parse file pointer: {}", value, e);
            return null;
        }

        final Response<ResponseBody> response = restClient.getFile(fileId).execute();

        if (response.isSuccessful()) {
            final ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.warn("Failed to load file from pointer: {} - Response code: {}. Body was null", value, response.code());
                return null;
            }

            long fileSize = responseBody.contentLength();
            if (fileSize < 1 && StringUtils.isNotBlank(fileSizeString)) {
                fileSize = Long.parseLong(fileSizeString);
            }

            log.debug("Successfully read file pointer from: {}, size: {}", value, fileSize);

            return new FileHandle(fileId, responseBody.byteStream(), fileSize);
        } else {
            log.warn("Failed to load file from pointer: {} - Response code: {}", value, response.code());
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
