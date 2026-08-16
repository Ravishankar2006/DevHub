package com.devhub.ai;

import com.devhub.ai.dto.AIProposedActionDto;
import com.devhub.common.ApiException;
import com.devhub.support.BaseIntegrationTest;
import com.devhub.tasks.TaskRepository;
import com.devhub.users.User;
import com.devhub.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AIProposedActionServiceTest extends BaseIntegrationTest {

    @Autowired
    private AIProposedActionService aiProposedActionService;

    @Autowired
    private AIProposedActionRepository aiProposedActionRepository;

    @Autowired
    private AIConversationRepository aiConversationRepository;

    @Autowired
    private AIMessageRepository aiMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User registerUser(String prefix) throws Exception {
        String email = uniqueEmail(prefix);
        registerAndLogin(email);
        return userRepository.findByEmail(email).orElseThrow();
    }

    private AIConversation newConversation(User user) {
        return aiConversationRepository.save(AIConversation.builder().user(user).build());
    }

    // AIProposedAction.createdAt is @Column(updatable = false), so a normal entity save()
    // can't backdate it (Hibernate silently omits the column from the UPDATE) -- go around
    // the ORM with a raw update, same as production data would age naturally.
    private void backdate(UUID proposalId, Instant createdAt) {
        jdbcTemplate.update("UPDATE ai_proposed_actions SET created_at = ? WHERE id = ?",
                Timestamp.from(createdAt), proposalId);
    }

    @Test
    void proposeCreatesRowOnValidPreview() throws Exception {
        User user = registerUser("proposal-create");
        AIConversation conversation = newConversation(user);

        AgentToolResult result = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Write docs"));

        assertThat(result.success()).isTrue();
        assertThat(result.payload().get("proposalId")).isNotNull();
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).isEmpty();

        List<AIProposedAction> rows = aiProposedActionRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversation.getId(), user.getId());
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getStatus()).isEqualTo(ProposalStatus.PENDING);
    }

    @Test
    void proposeDoesNotCreateRowOnValidationFailure() throws Exception {
        User user = registerUser("proposal-invalid");
        AIConversation conversation = newConversation(user);

        AgentToolResult result = aiProposedActionService.propose(user, conversation, "update_task", Map.of("title", "Nope", "status", "DONE"));

        assertThat(result.success()).isFalse();
        assertThat(aiProposedActionRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversation.getId(), user.getId())).isEmpty();
    }

    @Test
    void confirmExecutesTheRealMutation() throws Exception {
        User user = registerUser("proposal-confirm");
        AIConversation conversation = newConversation(user);

        AgentToolResult proposeResult = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Ship it"));
        UUID proposalId = UUID.fromString((String) proposeResult.payload().get("proposalId"));

        int messagesBefore = aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).size();

        AIProposedActionDto confirmed = aiProposedActionService.confirm(user, proposalId);

        assertThat(confirmed.getStatus()).isEqualTo("CONFIRMED");
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).hasSize(1);

        List<AIMessage> messages = aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        assertThat(messages).hasSize(messagesBefore + 1);
        assertThat(messages.get(messages.size() - 1).getContent()).startsWith("Confirmed:");
    }

    @Test
    void confirmOnAlreadyResolvedProposalThrows409() throws Exception {
        User user = registerUser("proposal-double-confirm");
        AIConversation conversation = newConversation(user);

        AgentToolResult proposeResult = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Once only"));
        UUID proposalId = UUID.fromString((String) proposeResult.payload().get("proposalId"));

        aiProposedActionService.confirm(user, proposalId);

        assertThatThrownBy(() -> aiProposedActionService.confirm(user, proposalId))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).hasSize(1);
    }

    @Test
    void rejectDoesNotExecute() throws Exception {
        User user = registerUser("proposal-reject");
        AIConversation conversation = newConversation(user);

        AgentToolResult proposeResult = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Never happens"));
        UUID proposalId = UUID.fromString((String) proposeResult.payload().get("proposalId"));

        AIProposedActionDto rejected = aiProposedActionService.reject(user, proposalId);

        assertThat(rejected.getStatus()).isEqualTo("REJECTED");
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).isEmpty();
    }

    @Test
    void expiredProposalCannotBeConfirmed() throws Exception {
        User user = registerUser("proposal-expired");
        AIConversation conversation = newConversation(user);

        AgentToolResult proposeResult = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Too late"));
        UUID proposalId = UUID.fromString((String) proposeResult.payload().get("proposalId"));

        backdate(proposalId, Instant.now().minus(31, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> aiProposedActionService.confirm(user, proposalId))
                .isInstanceOf(ApiException.class);
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).isEmpty();

        // The failed confirm's own transaction rolls back its expiry write along with the
        // exception it raised; a separate read (as listPending performs) independently
        // re-detects and persists the expiry, which is what actually matters -- the proposal
        // can never be confirmed once stale, regardless of exactly when EXPIRED lands in the DB.
        assertThat(aiProposedActionService.listPending(user, conversation.getId())).isEmpty();
        AIProposedAction reloaded = aiProposedActionRepository.findById(proposalId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ProposalStatus.EXPIRED);
    }

    @Test
    void listPendingExcludesExpiredAndResolved() throws Exception {
        User user = registerUser("proposal-list");
        AIConversation conversation = newConversation(user);

        AgentToolResult pending = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Still pending"));
        AgentToolResult toExpire = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Will expire"));
        AgentToolResult toConfirm = aiProposedActionService.propose(user, conversation, "create_task", Map.of("title", "Will confirm"));

        UUID expireId = UUID.fromString((String) toExpire.payload().get("proposalId"));
        backdate(expireId, Instant.now().minus(31, ChronoUnit.MINUTES));

        aiProposedActionService.confirm(user, UUID.fromString((String) toConfirm.payload().get("proposalId")));

        List<AIProposedActionDto> remaining = aiProposedActionService.listPending(user, conversation.getId());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId()).isEqualTo(UUID.fromString((String) pending.payload().get("proposalId")));
    }

    @Test
    void otherUserCannotConfirmOrRejectSomeoneElsesProposal() throws Exception {
        User owner = registerUser("proposal-owner");
        AIConversation conversation = newConversation(owner);
        AgentToolResult proposeResult = aiProposedActionService.propose(owner, conversation, "create_task", Map.of("title", "Mine"));
        UUID proposalId = UUID.fromString((String) proposeResult.payload().get("proposalId"));

        User other = registerUser("proposal-intruder");

        assertThatThrownBy(() -> aiProposedActionService.confirm(other, proposalId))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> aiProposedActionService.reject(other, proposalId))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
