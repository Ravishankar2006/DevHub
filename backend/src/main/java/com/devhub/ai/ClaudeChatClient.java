package com.devhub.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.devhub.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaudeChatClient {

    private static final String SYSTEM_PROMPT = """
            You are the AI Assistant built into DevHub, a personal productivity and career operating \
            system for solo developers. Help the user with project planning, learning, career and \
            resume guidance, and general questions. Only use information the user has actually shared \
            with you in this conversation, never invent details about their projects, resume, or job \
            applications. If you need information you don't have, ask for it.""";

    private static final long MAX_TOKENS = 4096L;

    private final AnthropicClient client;
    private final String model;

    public ClaudeChatClient(
            @Value("${devhub.ai.anthropic-api-key}") String apiKey,
            @Value("${devhub.ai.model:claude-opus-5}") String model) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.model = model;
    }

    public String sendMessage(List<AIMessage> history) {
        List<MessageParam> messages = history.stream()
                .map(message -> MessageParam.builder()
                        .role(message.getRole() == AIMessageRole.USER ? MessageParam.Role.USER : MessageParam.Role.ASSISTANT)
                        .content(message.getContent())
                        .build())
                .collect(Collectors.toList());

        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(MAX_TOKENS)
                .system(SYSTEM_PROMPT)
                .messages(messages)
                .build();

        try {
            Message response = client.messages().create(params);
            return response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(textBlock -> textBlock.text())
                    .collect(Collectors.joining("\n"))
                    .strip();
        } catch (AnthropicServiceException e) {
            throw new ApiException("AI assistant is temporarily unavailable, please try again shortly.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
