package io.dexi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;


public class DexiAppClient {

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    DexiAppClient(ObjectMapper objectMapper, Retrofit retrofit) {
        restClient = retrofit.create(RestClient.class);
        this.objectMapper = objectMapper;
    }

    public <T> T getActivationConfig(String activationId, Class<T> activationConfigType) throws IOException {
        final Response<ResponseBody> response = restClient.getActivationConfig(activationId).execute();

        if (!response.isSuccessful()) {
            return null;
        }

        return objectMapper.readValue(response.body().bytes(), activationConfigType);
    }

    public interface RestClient {
        @GET("apps/support/activations/{activationId}/configuration")
        Call<ResponseBody> getActivationConfig(@Path("activationId") String activationId);
    }

}
