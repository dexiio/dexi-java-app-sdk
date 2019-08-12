package io.dexi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;


public class DexiAppClient {

    private static final Logger log = LoggerFactory.getLogger(DexiAppClient.class);

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    DexiAppClient(ObjectMapper objectMapper, Retrofit retrofit) {
        restClient = retrofit.create(RestClient.class);
        this.objectMapper = objectMapper;
    }

    public <T> T getActivationConfig(String activationId, Class<T> activationConfigType) throws IOException {
        final Response<ResponseBody> response = restClient.getActivationConfig(activationId).execute();

        if (!response.isSuccessful()) {
            log.warn("Failed to get activation config for {}, status: {}, Error: {}",
                    activationId, response.code(), response.errorBody().string());
            return null;
        }

        return objectMapper.readValue(response.body().bytes(), activationConfigType);
    }

    public interface RestClient {
        @GET("apps/support/activations/{activationId}/configuration")
        Call<ResponseBody> getActivationConfig(@Path("activationId") String activationId);
    }

}
