package com.devhub.ai;

import com.devhub.ai.dto.AIConversationDetailDto;
import com.devhub.ai.dto.AIConversationSummaryDto;
import com.devhub.ai.dto.AIMapper;
import com.devhub.ai.dto.SendMessageRequest;
import com.devhub.common.ApiException;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIConversationService {

    private static final int TITLE_MAX_LENGTH = 50;

    private final AIConversationRepository aiConversationRepository;
    private final AIMessageRepository aiMessageRepository;
    private final ClaudeChatClient claudeChatClient;

    @Transactional
    public AIConversationDetailDto createConversation(User currentUser) {
        AIConversation conversation = aiConversationRepository.save(
                AIConversation.builder()
                        .user(currentUser)
                        .build());
        return AIMapper.toDetailDto(conversation, List.of());
    }

    @Transactional(readOnly = true)
    public List<AIConversationSummaryDto> listConversations(User currentUser) {
        return aiConversationRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId()).stream()
                .map(AIMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AIConversationDetailDto getConversation(User currentUser, UUID conversationId) {
        AIConversation conversation = getOwnedConversation(currentUser, conversationId);
        return AIMapper.toDetailDto(conversation, messagesFor(conversationId));
    }

    @Transactional
    public AIConversationDetailDto sendMessage(User currentUser, UUID conversationId, SendMessageRequest request) {
        AIConversation conversation = getOwnedConversation(currentUser, conversationId);

        AIMessage userMessage = aiMessageRepository.save(
                AIMessage.builder()
                        .conversation(conversation)
                        .role(AIMessageRole.USER)
                        .content(request.getContent())
                        .createdAt(Instant.now())
                        .build());

        List<AIMessage> history = messagesFor(conversationId);

        String assistantReply = claudeChatClient.sendMessage(history);

        aiMessageRepository.save(
                AIMessage.builder()
                        .conversation(conversation)
                        .role(AIMessageRole.ASSISTANT)
                        .content(assistantReply)
                        .createdAt(Instant.now())
                        .build());

        if (conversation.getTitle() == null) {
            conversation.setTitle(deriveTitle(userMessage.getContent()));
        }
        conversation = aiConversationRepository.save(conversation);

        return AIMapper.toDetailDto(conversation, messagesFor(conversationId));
    }

    @Transactional
    public void deleteConversation(User currentUser, UUID conversationId) {
        AIConversation conversation = getOwnedConversation(currentUser, conversationId);
        aiMessageRepository.deleteByConversationId(conversationId);
        aiConversationRepository.delete(conversation);
    }

    AIConversation getOwnedConversation(User currentUser, UUID conversationId) {
        AIConversation conversation = aiConversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException("Conversation not found", HttpStatus.NOT_FOUND));

        if (!conversation.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Conversation not found", HttpStatus.NOT_FOUND);
        }
        return conversation;
    }

    private List<AIMessage> messagesFor(UUID conversationId) {
        return aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    private String deriveTitle(String firstMessage) {
        String trimmed = firstMessage.strip();
        if (trimmed.length() <= TITLE_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, TITLE_MAX_LENGTH).strip() + "...";
    }
}
