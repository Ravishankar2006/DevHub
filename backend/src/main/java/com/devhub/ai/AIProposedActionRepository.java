package com.devhub.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIProposedActionRepository extends JpaRepository<AIProposedAction, UUID> {
    List<AIProposedAction> findByConversationIdAndUserIdOrderByCreatedAtAsc(UUID conversationId, UUID userId);
}
