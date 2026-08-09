package com.devhub.ai;

import com.devhub.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class GeminiEmbeddingClient {

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;
    private final String apiKey;
    private final String model;

    public GeminiEmbeddingClient(
            @Value("${devhub.ai.gemini-api-key}") String apiKey,
            @Value("${devhub.ai.embedding-model:gemini-embedding-001}") String model,
            RetryTemplate externalCallRetryTemplate) {
        this.apiKey = apiKey;
        this.model = model;
        this.retryTemplate = externalCallRetryTemplate;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public float[] embed(String text, String taskType) {
        EmbedRequest request = new EmbedRequest(new EmbedContent(List.of(new EmbedPart(text))), taskType);

        try {
            EmbedResponse response = retryTemplate.execute(ctx -> restClient.post()
                    .uri("/models/{model}:embedContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(EmbedResponse.class));

            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                throw new ApiException("Embedding service returned an empty response.", HttpStatus.SERVICE_UNAVAILABLE);
            }

            List<Float> values = response.embedding().values();
            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = values.get(i);
            }
            return result;
        } catch (RestClientException e) {
            throw new ApiException("Embedding service is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record EmbedRequest(EmbedContent content, String taskType) {}

    private record EmbedContent(List<EmbedPart> parts) {}

    private record EmbedPart(String text) {}

    private record EmbedResponse(EmbedValues embedding) {}

    private record EmbedValues(List<Float> values) {}
}
