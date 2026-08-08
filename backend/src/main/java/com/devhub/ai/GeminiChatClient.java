package com.devhub.ai;

import com.devhub.common.ApiException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiChatClient {

    private static final String SYSTEM_PROMPT = """
            You are the AI Assistant built into DevHub, a personal productivity and career operating \
            system for solo developers. Help the user with project planning, learning, career and \
            resume guidance, and general questions. Only use information the user has actually shared \
            with you in this conversation, never invent details about their projects, resume, or job \
            applications. If you need information you don't have, ask for it.""";

    private static final String RESUME_REVIEW_PROMPT = """
            You are an ATS-style resume reviewer. Analyze the attached resume PDF and return:
            - an overall score from 0 to 100 reflecting how well it would perform in an applicant \
            tracking system and with a human recruiter,
            - a one-to-two sentence summary of your overall impression,
            - a list of specific issues (weak or vague bullet points, missing keywords, formatting \
            problems, missing sections),
            - a list of specific, actionable improvement suggestions.
            Be concrete and reference actual content from the resume, not generic advice.""";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiChatClient(
            @Value("${devhub.ai.gemini-api-key}") String apiKey,
            @Value("${devhub.ai.model:gemini-flash-latest}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
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

    public ResumeAnalysis analyzeResume(byte[] pdfBytes) {
        AnalysisContent content = new AnalysisContent("user", List.of(
                new AnalysisPart(RESUME_REVIEW_PROMPT, null),
                new AnalysisPart(null, new InlineData("application/pdf", Base64.getEncoder().encodeToString(pdfBytes)))));

        ReviewSchema schema = new ReviewSchema(
                "OBJECT",
                Map.of(
                        "score", new SchemaProperty("INTEGER", null),
                        "summary", new SchemaProperty("STRING", null),
                        "issues", new SchemaProperty("ARRAY", new SchemaProperty("STRING", null)),
                        "suggestions", new SchemaProperty("ARRAY", new SchemaProperty("STRING", null))),
                List.of("score", "summary", "issues", "suggestions"));

        AnalysisRequest request = new AnalysisRequest(
                List.of(content),
                new ReviewGenerationConfig("application/json", schema));

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

            String json = response.candidates().get(0).content().parts().stream()
                    .map(GeminiPart::text)
                    .collect(Collectors.joining())
                    .strip();

            return objectMapper.readValue(json, ResumeAnalysis.class);
        } catch (RestClientException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            throw new ApiException("AI assistant returned an unreadable response.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public record ResumeAnalysis(int score, String summary, List<String> issues, List<String> suggestions) {}

    private record GeminiRequest(List<GeminiContent> contents, GeminiSystemInstruction systemInstruction) {}

    private record GeminiSystemInstruction(List<GeminiPart> parts) {}

    private record GeminiContent(String role, List<GeminiPart> parts) {}

    private record GeminiPart(String text) {}

    private record GeminiResponse(List<GeminiCandidate> candidates) {}

    private record GeminiCandidate(GeminiResponseContent content) {}

    private record GeminiResponseContent(List<GeminiPart> parts, String role) {}

    private record AnalysisRequest(List<AnalysisContent> contents, ReviewGenerationConfig generationConfig) {}

    private record AnalysisContent(String role, List<AnalysisPart> parts) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AnalysisPart(String text, InlineData inlineData) {}

    private record InlineData(String mimeType, String data) {}

    private record ReviewGenerationConfig(String responseMimeType, ReviewSchema responseSchema) {}

    private record ReviewSchema(String type, Map<String, SchemaProperty> properties, List<String> required) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SchemaProperty(String type, SchemaProperty items) {}
}
