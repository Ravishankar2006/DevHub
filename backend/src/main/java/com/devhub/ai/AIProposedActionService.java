package com.devhub.ai;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
import com.devhub.ai.dto.AIMapper;
import com.devhub.ai.dto.AIProposedActionDto;
import com.devhub.common.ApiException;
import com.devhub.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIProposedActionService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final AIProposedActionRepository aiProposedActionRepository;
    private final AIMessageRepository aiMessageRepository;
    private final AgentToolExecutor agentToolExecutor;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AgentToolResult propose(User currentUser, AIConversation conversation, String toolName, Map<String, Object> args) {
        AgentToolResult preview = agentToolExecutor.preview(currentUser, toolName, args);
        if (!preview.success()) {
            return preview;
        }

        String summary = String.valueOf(preview.payload().get("summary"));
        AIProposedAction proposal = AIProposedAction.builder()
                .conversation(conversation)
                .user(currentUser)
                .actionType(AiActionType.fromToolName(toolName))
                .payload(writePayload(args))
                .summary(summary)
                .destructive(toolName.startsWith("delete_"))
                .build();
        proposal = aiProposedActionRepository.save(proposal);

        return AgentToolResult.ok(Map.of(
                "status", "proposed",
                "proposalId", proposal.getId().toString(),
                "summary", summary));
    }

    @Transactional
    public AIProposedActionDto confirm(User currentUser, UUID proposalId) {
        AIProposedAction proposal = getOwnedPending(currentUser, proposalId);

        Map<String, Object> args = readPayload(proposal.getPayload());
        AgentToolResult result = agentToolExecutor.execute(currentUser, proposal.getActionType().toToolName(), args);
        if (!result.success()) {
            throw new ApiException(String.valueOf(result.payload().get("message")), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        proposal.setStatus(ProposalStatus.CONFIRMED);
        proposal.setResolvedAt(Instant.now());
        proposal = aiProposedActionRepository.save(proposal);

        Object id = result.payload().get("id");
        activityLogService.log(currentUser, ActivityLogSource.AI_AGENT, proposal.getActionType().name(),
                id != null ? String.valueOf(id) : null, proposal.getSummary());

        appendMessage(proposal.getConversation(), "Confirmed: " + proposal.getSummary());
        return AIMapper.toProposalDto(proposal);
    }

    @Transactional
    public AIProposedActionDto reject(User currentUser, UUID proposalId) {
        AIProposedAction proposal = getOwnedPending(currentUser, proposalId);

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setResolvedAt(Instant.now());
        proposal = aiProposedActionRepository.save(proposal);

        appendMessage(proposal.getConversation(), "Cancelled: " + proposal.getSummary());
        return AIMapper.toProposalDto(proposal);
    }

    @Transactional
    public List<AIProposedActionDto> listPending(User currentUser, UUID conversationId) {
        return aiProposedActionRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversationId, currentUser.getId())
                .stream()
                .map(this::expireIfStale)
                .filter(p -> p.getStatus() == ProposalStatus.PENDING)
                .map(AIMapper::toProposalDto)
                .collect(Collectors.toList());
    }

    private AIProposedAction getOwnedPending(User currentUser, UUID proposalId) {
        AIProposedAction proposal = aiProposedActionRepository.findById(proposalId)
                .orElseThrow(() -> new ApiException("Proposal not found", HttpStatus.NOT_FOUND));

        if (!proposal.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Proposal not found", HttpStatus.NOT_FOUND);
        }

        proposal = expireIfStale(proposal);
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ApiException("This proposal is no longer pending (" + proposal.getStatus() + ").", HttpStatus.CONFLICT);
        }
        return proposal;
    }

    private AIProposedAction expireIfStale(AIProposedAction proposal) {
        if (proposal.getStatus() == ProposalStatus.PENDING && proposal.getCreatedAt().isBefore(Instant.now().minus(TTL))) {
            proposal.setStatus(ProposalStatus.EXPIRED);
            proposal.setResolvedAt(Instant.now());
            return aiProposedActionRepository.save(proposal);
        }
        return proposal;
    }

    private void appendMessage(AIConversation conversation, String content) {
        aiMessageRepository.save(AIMessage.builder()
                .conversation(conversation)
                .role(AIMessageRole.ASSISTANT)
                .content(content)
                .createdAt(Instant.now())
                .build());
    }

    private String writePayload(Map<String, Object> args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            throw new ApiException("Could not serialize proposed action.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new ApiException("Could not read proposed action payload.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
