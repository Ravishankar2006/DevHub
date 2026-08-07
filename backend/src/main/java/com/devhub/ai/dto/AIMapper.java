package com.devhub.ai.dto;

import com.devhub.ai.AIConversation;
import com.devhub.ai.AIMessage;

import java.util.List;
import java.util.stream.Collectors;

public class AIMapper {

    public static AIMessageDto toMessageDto(AIMessage message) {
        if (message == null) return null;

        return AIMessageDto.builder()
                .id(message.getId())
                .role(message.getRole().name())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public static AIConversationSummaryDto toSummaryDto(AIConversation conversation) {
        if (conversation == null) return null;

        return AIConversationSummaryDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public static AIConversationDetailDto toDetailDto(AIConversation conversation, List<AIMessage> messages) {
        if (conversation == null) return null;

        List<AIMessageDto> messageDtos = messages == null
                ? List.of()
                : messages.stream().map(AIMapper::toMessageDto).collect(Collectors.toList());

        return AIConversationDetailDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(messageDtos)
                .build();
    }
}
