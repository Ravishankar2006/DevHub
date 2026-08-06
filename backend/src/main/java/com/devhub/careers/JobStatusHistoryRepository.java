package com.devhub.careers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobStatusHistoryRepository extends JpaRepository<JobStatusHistory, UUID> {
    List<JobStatusHistory> findByJobApplicationIdOrderByChangedAtDesc(UUID jobApplicationId);
    void deleteByJobApplicationId(UUID jobApplicationId);
}
