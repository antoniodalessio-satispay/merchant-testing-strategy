package com.merchant.testing.service.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryApiService {

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public DictionaryApiService(OkHttpClient httpClient, @Value("external.api.base-url") String baseUrl, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, String>> getDictionary(String word) {
        try {
            return List.of(objectMapper.readValue(fetchData("/api/v2/entries/en/" + word), new TypeReference<Map<String, String>[]>() {
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchData(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            return response.body() != null ? response.body().string() : "";
        }
    }
}
