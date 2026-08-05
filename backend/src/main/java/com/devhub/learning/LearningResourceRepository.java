package com.devhub.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, UUID> {
    List<LearningResource> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<LearningResource> findByUserIdAndStatusOrderByUpdatedAtDesc(UUID userId, LearningStatus status);
    List<LearningResource> findByUserIdAndResourceTypeOrderByUpdatedAtDesc(UUID userId, LearningResourceType resourceType);
    List<LearningResource> findByUserIdAndStatusAndResourceTypeOrderByUpdatedAtDesc(
            UUID userId, LearningStatus status, LearningResourceType resourceType);
    long countByUserIdAndStatusIn(UUID userId, List<LearningStatus> statuses);
}
