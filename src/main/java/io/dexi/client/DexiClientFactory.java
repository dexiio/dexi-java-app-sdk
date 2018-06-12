package io.dexi.client;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DexiClientFactory {
    public static final String DEFAULT_BASE_URL = "https://api.dexi.io/";

    /**
     * Tell dexi that we're behaving as an app
     */
    public static final String AUTH_TYPE = "APP";


    private final Cache<String, DexiClient> clientCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DexiAuth auth;

    private final String baseUrl;

    public DexiClientFactory(DexiAuth auth) {
        this(DEFAULT_BASE_URL, auth);
    }

    public DexiClientFactory(String baseUrl, DexiAuth auth) {
        this.baseUrl = baseUrl;
        this.auth = auth;

        setupObjectMapper();
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
            throw new RuntimeException("Failed to instantiate dexi client", e);
        }
    }


    public class DexiClient {

        private final String activationId;

        private final Retrofit retrofit;

        private final DexiFileClient fileClient;

        private final DexiAppClient appClient;

        private DexiClient(String activationId) {
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
