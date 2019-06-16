package io.dexi.client;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.dexi.config.DexiConfig;
import io.dexi.service.DexiPayloadHeaders;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class DexiClientFactory {
    private static final Logger log = LoggerFactory.getLogger(DexiClientFactory.class);

    /**
     * Tell dexi that we're behaving as an app
     */
    public static final String AUTH_TYPE = "APP";


    protected final Cache<String, DexiClient> clientCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, Object> activationConfigCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final DexiAuth auth;

    protected final String baseUrl;

    public DexiClientFactory() {
        this.baseUrl = safeBaseUrl(DexiConfig.getBaseUrl());
        this.auth = DexiAuth.from(DexiConfig.getAccount(), DexiConfig.getApiKey());
    }

    public DexiClientFactory(DexiAuth auth) {
        this(DexiConfig.getBaseUrl(), auth);
    }

    public DexiClientFactory(String baseUrl, DexiAuth auth) {
        this.baseUrl = safeBaseUrl(baseUrl);
        this.auth = auth;

        setupObjectMapper();
    }

    private String safeBaseUrl(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            return baseUrl + "/";
        }

        return baseUrl;
    }

    protected void setupObjectMapper() {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public DexiClient create(final String activationId) {
        assert activationId != null && !activationId.isEmpty();

        try {
            return clientCache.get(activationId, () -> new DexiClient(activationId));
        } catch (ExecutionException e) {
            log.error("Failed to instantiate dexi client", e);
            throw new RuntimeException("Failed to instantiate dexi client", e);
        }
    }

    public <T> T getActivationConfig(String activationId, Class<T> activationClass) throws DexiClientException {
        try {
            assert activationId != null && !activationId.isEmpty();

            final T activationConfig = (T) activationConfigCache.get(activationId,
                () -> create(activationId).apps().getActivationConfig(activationId, activationClass)
            );

            if (activationConfig == null) {
                return null;
            }

            return activationConfig;
        } catch (Exception e) {
            throw new DexiClientException("Could not get configuration for app activation", e);
        }
    }

    public <T> T getConfiguration(HttpRequest request, Class<T> clz) throws IOException {

        final String json = request.getHeader(DexiPayloadHeaders.CONFIGURATION);
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        return objectMapper.readValue(json, clz);
    }

    public interface HttpRequest {
        public String getHeader(String headerName);
    }

    public class DexiClient {

        protected final String activationId;

        protected final Retrofit retrofit;

        protected final DexiFileClient fileClient;

        protected final DexiAppClient appClient;

        protected DexiClient(String activationId) {
            this.activationId = activationId;

            this.retrofit = buildRetrofit(auth, baseUrl);

            this.fileClient = new DexiFileClient(retrofit);

            this.appClient = new DexiAppClient(objectMapper, retrofit);
        }


        /**
         * Client for downloading files from dexi
         *
         * @return
         */
        public DexiFileClient files() {
            return fileClient;
        }


        public DexiAppClient apps() {
            return appClient;
        }

        /**
         * Create retrofit client which will send all the proper dexi headers etc.
         *
         * @param service
         * @param <T>
         * @return
         */
        public <T> T create(Class<T> service) {
            return retrofit.create(service);
        }

        /**
         * Create the retrofit instance. Override to add custom implementation
         *
         * @param auth
         * @param baseUrl
         * @return
         */
        protected Retrofit buildRetrofit(final DexiAuth auth, String baseUrl) {
            return new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .client(buildClient(auth))
                    .build();
        }

        /**
         * Build OkHttpClient instance - override to add custom implementation
         *
         * @param auth
         * @return
         */
        protected OkHttpClient buildClient(final DexiAuth auth) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("User-Agent", DexiAuth.USER_AGENT)
                        .header(DexiAuth.HEADER_AUTH_TYPE, AUTH_TYPE)
                        .header(DexiAuth.HEADER_ACCOUNT, auth.getAccountId())
                        .header(DexiAuth.HEADER_ACCESS, auth.getAccess())
                        .header(DexiAuth.HEADER_ACTIVATION, activationId)
                        .build();

                return chain.proceed(request);
            });

            return httpClient.build();
        }
    }
}
