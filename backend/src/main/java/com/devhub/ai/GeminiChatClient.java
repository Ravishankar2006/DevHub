package com.devhub.ai;

import com.devhub.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiChatClient {

    private static final String SYSTEM_PROMPT = """
            You are the AI Assistant built into DevHub, a personal productivity and career operating \
            system for solo developers. Help the user with project planning, learning, career and \
            resume guidance, and general questions. Only use information the user has actually shared \
            with you in this conversation, never invent details about their projects, resume, or job \
            applications. If you need information you don't have, ask for it.""";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiChatClient(
            @Value("${devhub.ai.gemini-api-key}") String apiKey,
            @Value("${devhub.ai.model:gemini-flash-latest}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public String sendMessage(List<AIMessage> history) {
        List<GeminiContent> contents = history.stream()
                .map(message -> new GeminiContent(
                        message.getRole() == AIMessageRole.USER ? "user" : "model",
                        List.of(new GeminiPart(message.getContent()))))
                .collect(Collectors.toList());

        GeminiRequest request = new GeminiRequest(
                contents,
                new GeminiSystemInstruction(List.of(new GeminiPart(SYSTEM_PROMPT))));

        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new ApiException("AI assistant returned an empty response.", HttpStatus.SERVICE_UNAVAILABLE);
            }

            return response.candidates().get(0).content().parts().stream()
                    .map(GeminiPart::text)
                    .collect(Collectors.joining("\n"))
                    .strip();
        } catch (RestClientException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record GeminiRequest(List<GeminiContent> contents, GeminiSystemInstruction systemInstruction) {}

    private record GeminiSystemInstruction(List<GeminiPart> parts) {}

    private record GeminiContent(String role, List<GeminiPart> parts) {}

    private record GeminiPart(String text) {}

    private record GeminiResponse(List<GeminiCandidate> candidates) {}

    private record GeminiCandidate(GeminiResponseContent content) {}

    private record GeminiResponseContent(List<GeminiPart> parts, String role) {}
}
