package com.devhub.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubRepoRepository extends JpaRepository<GitHubRepo, UUID> {
    List<GitHubRepo> findByGithubAccountIdOrderByPushedAtDesc(UUID githubAccountId);
    Optional<GitHubRepo> findByGithubAccountIdAndGithubRepoId(UUID githubAccountId, Long githubRepoId);
    void deleteByGithubAccountId(UUID githubAccountId);
}
