package com.devhub.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<Project> findByUserIdAndArchivedOrderByUpdatedAtDesc(UUID userId, boolean archived);
    long countByUserIdAndArchivedFalse(UUID userId);
}
