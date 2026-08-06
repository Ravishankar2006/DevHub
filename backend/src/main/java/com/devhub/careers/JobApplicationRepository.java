package com.devhub.careers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<JobApplication> findByUserIdAndStatusOrderByUpdatedAtDesc(UUID userId, JobApplicationStatus status);
    List<JobApplication> findByUserIdAndCompanyIdOrderByUpdatedAtDesc(UUID userId, UUID companyId);
    List<JobApplication> findByUserIdAndStatusAndCompanyIdOrderByUpdatedAtDesc(
            UUID userId, JobApplicationStatus status, UUID companyId);
    List<JobApplication> findByResumeId(UUID resumeId);
}
