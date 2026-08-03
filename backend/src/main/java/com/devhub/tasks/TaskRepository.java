package com.devhub.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserIdOrderByPositionAsc(UUID userId);
    List<Task> findByUserIdAndProjectIdOrderByPositionAsc(UUID userId, UUID projectId);
    List<Task> findByUserIdAndStatusOrderByPositionAsc(UUID userId, TaskStatus status);
    List<Task> findByUserIdAndProjectIdAndStatusOrderByPositionAsc(UUID userId, UUID projectId, TaskStatus status);
    List<Task> findByMilestoneIdOrderByPositionAsc(UUID milestoneId);

    void deleteByProjectId(UUID projectId);

    long countByProjectId(UUID projectId);
    long countByProjectIdAndStatus(UUID projectId, TaskStatus status);
    long countByMilestoneId(UUID milestoneId);
    long countByMilestoneIdAndStatus(UUID milestoneId, TaskStatus status);
    long countByUserIdAndStatus(UUID userId, TaskStatus status);
    long countByUserIdAndStatusNot(UUID userId, TaskStatus status);
}
