package com.devhub.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubAccountRepository extends JpaRepository<GitHubAccount, UUID> {
    Optional<GitHubAccount> findByUserId(UUID userId);

    @Query("SELECT a.user.id FROM GitHubAccount a")
    List<UUID> findAllUserIds();
}
