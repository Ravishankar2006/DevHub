package com.devhub.resumes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<Resume> findByUserIdAndLabelOrderByUpdatedAtDesc(UUID userId, String label);
}
