package com.devhub.goals;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<Goal> findByUserIdAndStatusOrderByUpdatedAtDesc(UUID userId, GoalStatus status);
    List<Goal> findByProjectId(UUID projectId);
    long countByUserIdAndStatus(UUID userId, GoalStatus status);
}
