package com.devhub.ai;

import com.devhub.common.ApiException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GeminiChatClient {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are the AI Assistant built into DevHub, a personal productivity and career operating \
            system for solo developers. Help the user with project planning, learning, career and \
            resume guidance, and general questions. Only use information the user has actually shared \
            with you in this conversation, never invent details about their projects, resume, or job \
            applications. If you need information you don't have, ask for it.

            You can also act on the user's behalf using the tools provided: creating tasks, habits, \
            notes, calendar events, goals, and projects. When the user asks you to create or schedule \
            something, actually call the matching tool -- never just describe what you would do or \
            claim something was created without calling the tool. If required information is missing \
            or genuinely ambiguous (for example no title given, or an unclear date), ask a clarifying \
            question in plain text instead of guessing or calling a tool with made-up values. After a \
            tool call completes, summarize in plain language exactly what was created, using the \
            actual result data -- never invent details the tool result didn't confirm. If a tool call \
            fails, tell the user what went wrong. Resolve relative dates and times (\"tomorrow\", \
            \"next Friday\", \"in two hours\") against the current date/time given below, and express \
            dates back to the user in plain language, not raw ISO timestamps. Write in plain text only \
            -- no markdown, no asterisks, no bold/italic formatting.

            Current date/time (UTC): %s""";

    private static final String RESUME_REVIEW_PROMPT = """
            You are an ATS-style resume reviewer. Analyze the attached resume PDF and return:
            - an overall score from 0 to 100 reflecting how well it would perform in an applicant \
            tracking system and with a human recruiter,
            - a one-to-two sentence summary of your overall impression,
            - a list of specific issues (weak or vague bullet points, missing keywords, formatting \
            problems, missing sections),
            - a list of specific, actionable improvement suggestions.
            Be concrete and reference actual content from the resume, not generic advice. Write in \
            plain text only -- no markdown, no asterisks, no bold/italic formatting.""";

    private static final String PDF_TEXT_EXTRACTION_PROMPT = """
            Extract all readable text content from the attached PDF, in reading order. Return only \
            the extracted text with no commentary, no markdown formatting, and no added headers.""";

    private static final String DAILY_BRIEF_PROMPT = """
            You are writing a short daily briefing for a solo developer inside DevHub. Given the \
            structured summary of their tasks, goals, upcoming deadlines, learning items, \
            calendar events, and GitHub/LeetCode coding activity below, write a concise, motivating \
            daily brief (3-5 sentences of plain \
            prose -- do not use a bullet or numbered list). Prioritize what's most time-sensitive \
            (today and tomorrow) first. Only reference items actually present in the data below -- \
            never invent tasks, dates, or events. If there is very little going on, say so plainly \
            and briefly. Write in plain text only -- no markdown, no asterisks, no bold/italic \
            formatting, no headers.""";

    /**
     * Gemini's v1beta REST API rejects role "function" for turns carrying a functionResponse part
     * ("Role 'function' is not supported... valid role: SYSTEM, ... USER, ... MODEL, USER") -- role
     * "user" is what it actually expects there. Confirmed empirically against the live API (curl)
     * on 2026-08-15, not assumed from documentation.
     */
    private static final String FUNCTION_RESPONSE_ROLE = "user";
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RetryTemplate retryTemplate;
    private final String apiKey;
    private final String model;

    public GeminiChatClient(
            @Value("${devhub.ai.gemini-api-key}") String apiKey,
            @Value("${devhub.ai.model:gemini-flash-latest}") String model,
            ObjectMapper objectMapper,
            RetryTemplate externalCallRetryTemplate) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.retryTemplate = externalCallRetryTemplate;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    @FunctionalInterface
    public interface ToolInvoker {
        AgentToolResult invoke(String toolName, Map<String, Object> arguments);
    }

    public String sendAgentMessage(List<AIMessage> history, ToolInvoker toolInvoker) {
        List<GeminiContent> contents = new ArrayList<>(history.stream()
                .map(message -> new GeminiContent(
                        message.getRole() == AIMessageRole.USER ? "user" : "model",
                        List.of(GeminiPart.text(message.getContent()))))
                .toList());

        List<GeminiTool> tools = buildToolDeclarations();
        GeminiSystemInstruction systemInstruction =
                new GeminiSystemInstruction(List.of(GeminiPart.text(buildSystemPrompt())));

        for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
            GeminiRequest request = new GeminiRequest(contents, systemInstruction, tools);
            GeminiResponse response = callGenerateContent(request);
            List<GeminiPart> parts = response.candidates().get(0).content().parts();

            List<GeminiPart> calls = parts.stream().filter(p -> p.functionCall() != null).toList();
            if (calls.isEmpty()) {
                return parts.stream()
                        .map(GeminiPart::text)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("\n"))
                        .strip();
            }

            contents.add(new GeminiContent("model", parts));

            List<GeminiPart> responseParts = new ArrayList<>();
            for (GeminiPart call : calls) {
                AgentToolResult result = toolInvoker.invoke(call.functionCall().name(), call.functionCall().args());
                responseParts.add(GeminiPart.functionResponse(call.functionCall().name(), result.payload()));
            }
            contents.add(new GeminiContent(FUNCTION_RESPONSE_ROLE, responseParts));
        }

        throw new ApiException("AI assistant could not finish the request after multiple tool calls.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public String generateDailyBrief(String dataSummary) {
        GeminiContent content = new GeminiContent("user", List.of(GeminiPart.text(dataSummary)));

        GeminiRequest request = new GeminiRequest(
                List.of(content),
                new GeminiSystemInstruction(List.of(GeminiPart.text(DAILY_BRIEF_PROMPT))));

        GeminiResponse response = callGenerateContent(request);
        return response.candidates().get(0).content().parts().stream()
                .map(GeminiPart::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"))
                .strip();
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
            GeminiResponse response = retryTemplate.execute(ctx -> restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class));

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new ApiException("AI assistant returned an empty response.", HttpStatus.SERVICE_UNAVAILABLE);
            }

            String json = response.candidates().get(0).content().parts().stream()
                    .map(GeminiPart::text)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining())
                    .strip();

            return objectMapper.readValue(json, ResumeAnalysis.class);
        } catch (RestClientException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("AI assistant returned an unreadable response.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public String extractPdfText(byte[] pdfBytes) {
        AnalysisContent content = new AnalysisContent("user", List.of(
                new AnalysisPart(PDF_TEXT_EXTRACTION_PROMPT, null),
                new AnalysisPart(null, new InlineData("application/pdf", Base64.getEncoder().encodeToString(pdfBytes)))));

        AnalysisRequest request = new AnalysisRequest(List.of(content), null);

        try {
            GeminiResponse response = retryTemplate.execute(ctx -> restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class));

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new ApiException("AI assistant returned an empty response.", HttpStatus.SERVICE_UNAVAILABLE);
            }

            return response.candidates().get(0).content().parts().stream()
                    .map(GeminiPart::text)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"))
                    .strip();
        } catch (RestClientException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private GeminiResponse callGenerateContent(Object request) {
        try {
            GeminiResponse response = retryTemplate.execute(ctx -> restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class));

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new ApiException("AI assistant returned an empty response.", HttpStatus.SERVICE_UNAVAILABLE);
            }
            return response;
        } catch (RestClientException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String buildSystemPrompt() {
        return SYSTEM_PROMPT_TEMPLATE.formatted(Instant.now());
    }

    private static List<GeminiTool> buildToolDeclarations() {
        return List.of(new GeminiTool(List.of(
                new FunctionDeclaration(
                        "create_task",
                        "Create a new task for the user. Tasks can optionally belong to an existing project.",
                        new ParamSchema("OBJECT", Map.of(
                                "title", new ParamProperty("STRING", "Short title of the task.", null, null),
                                "description", new ParamProperty("STRING", "Optional longer description.", null, null),
                                "projectName", new ParamProperty("STRING", "Name of an existing project this task belongs to, if the user mentioned one; omit for a standalone task.", null, null),
                                "status", new ParamProperty("STRING", "Task status; defaults to TODO if omitted.", List.of("TODO", "IN_PROGRESS", "DONE"), null),
                                "priority", new ParamProperty("STRING", "Task priority; defaults to MEDIUM if omitted.", List.of("LOW", "MEDIUM", "HIGH"), null),
                                "dueDate", new ParamProperty("STRING", "Due date in ISO-8601 format YYYY-MM-DD, only if mentioned.", null, null)),
                                List.of("title"))),
                new FunctionDeclaration(
                        "create_habit",
                        "Create a new recurring habit for the user, optionally linked to an existing goal.",
                        new ParamSchema("OBJECT", Map.of(
                                "title", new ParamProperty("STRING", "Habit title.", null, null),
                                "goalName", new ParamProperty("STRING", "Name of an existing goal this habit supports, if mentioned.", null, null),
                                "frequency", new ParamProperty("STRING", "How often the habit repeats; defaults to DAILY if omitted.", List.of("DAILY", "WEEKLY"), null)),
                                List.of("title"))),
                new FunctionDeclaration(
                        "create_note",
                        "Create a new note for the user, optionally inside an existing folder and/or tagged.",
                        new ParamSchema("OBJECT", Map.of(
                                "title", new ParamProperty("STRING", "Note title.", null, null),
                                "content", new ParamProperty("STRING", "Note body text.", null, null),
                                "tags", new ParamProperty("ARRAY", "Freeform tags for the note.", null, new ParamProperty("STRING", null, null, null)),
                                "folderName", new ParamProperty("STRING", "Name of an existing folder, if mentioned.", null, null)),
                                List.of("title"))),
                new FunctionDeclaration(
                        "create_calendar_event",
                        "Create a new calendar event or appointment for the user.",
                        new ParamSchema("OBJECT", Map.of(
                                "title", new ParamProperty("STRING", "Event title.", null, null),
                                "description", new ParamProperty("STRING", "Optional details.", null, null),
                                "category", new ParamProperty("STRING", "Event category; defaults to OTHER if omitted.", List.of("INTERVIEW", "TASK", "STUDY_BLOCK", "DEADLINE", "MEETING", "OTHER"), null),
                                "startTime", new ParamProperty("STRING", "Start date-time as an ISO-8601 instant, e.g. 2026-08-20T15:00:00Z. Resolve relative phrases against the current date/time given in context.", null, null),
                                "endTime", new ParamProperty("STRING", "End date-time, same ISO-8601 instant format, if mentioned.", null, null),
                                "allDay", new ParamProperty("BOOLEAN", "True if this is an all-day event; defaults to false.", null, null),
                                "location", new ParamProperty("STRING", "Optional location.", null, null)),
                                List.of("title", "startTime"))),
                new FunctionDeclaration(
                        "create_goal",
                        "Create a new goal for the user, optionally linked to an existing project.",
                        new ParamSchema("OBJECT", Map.of(
                                "title", new ParamProperty("STRING", "Goal title.", null, null),
                                "description", new ParamProperty("STRING", "Optional details.", null, null),
                                "type", new ParamProperty("STRING", "Goal timeframe; defaults to MONTHLY if omitted.", List.of("DAILY", "WEEKLY", "MONTHLY", "CAREER"), null),
                                "targetDate", new ParamProperty("STRING", "Target date in ISO-8601 format YYYY-MM-DD, if mentioned.", null, null),
                                "progressPercent", new ParamProperty("INTEGER", "Progress from 0 to 100, if mentioned.", null, null),
                                "projectName", new ParamProperty("STRING", "Name of an existing project this goal supports, if mentioned.", null, null)),
                                List.of("title"))),
                new FunctionDeclaration(
                        "create_project",
                        "Create a new project for the user.",
                        new ParamSchema("OBJECT", Map.of(
                                "name", new ParamProperty("STRING", "Project name.", null, null),
                                "description", new ParamProperty("STRING", "Optional description.", null, null),
                                "repoUrl", new ParamProperty("STRING", "Optional repository URL.", null, null),
                                "liveUrl", new ParamProperty("STRING", "Optional live/deployed URL.", null, null),
                                "roadmap", new ParamProperty("STRING", "Optional roadmap or plan text.", null, null),
                                "stackTags", new ParamProperty("ARRAY", "Technologies used.", null, new ParamProperty("STRING", null, null, null)),
                                "status", new ParamProperty("STRING", "Project status; defaults to PLANNING if omitted.", List.of("PLANNING", "IN_PROGRESS", "ON_HOLD", "COMPLETED"), null)),
                                List.of("name")))
        )));
    }

    public record ResumeAnalysis(int score, String summary, List<String> issues, List<String> suggestions) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record GeminiRequest(List<GeminiContent> contents, GeminiSystemInstruction systemInstruction, List<GeminiTool> tools) {
        GeminiRequest(List<GeminiContent> contents, GeminiSystemInstruction systemInstruction) {
            this(contents, systemInstruction, null);
        }
    }

    private record GeminiSystemInstruction(List<GeminiPart> parts) {}

    private record GeminiContent(String role, List<GeminiPart> parts) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record GeminiPart(String text, FunctionCall functionCall, FunctionResponse functionResponse, String thoughtSignature) {
        static GeminiPart text(String text) {
            return new GeminiPart(text, null, null, null);
        }

        static GeminiPart functionResponse(String name, Map<String, Object> response) {
            return new GeminiPart(null, null, new FunctionResponse(name, response), null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record FunctionCall(String name, Map<String, Object> args, String id) {}

    private record FunctionResponse(String name, Map<String, Object> response) {}

    private record GeminiTool(List<FunctionDeclaration> functionDeclarations) {}

    private record FunctionDeclaration(String name, String description, ParamSchema parameters) {}

    private record ParamSchema(String type, Map<String, ParamProperty> properties, List<String> required) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record ParamProperty(String type, String description, @com.fasterxml.jackson.annotation.JsonProperty("enum") List<String> enumValues, ParamProperty items) {}

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
