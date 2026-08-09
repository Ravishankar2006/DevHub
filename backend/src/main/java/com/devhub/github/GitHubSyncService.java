package com.devhub.github;

import com.devhub.common.ApiException;
import com.devhub.jobs.AiJob;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.jobs.dto.AiJobMapper;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSyncService {

    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final GitHubApiClient gitHubApiClient;
    private final AiJobService aiJobService;

    @Transactional
    public AiJobDto triggerManualSync(User currentUser) {
        gitHubAccountRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ApiException("Connect your GitHub account first.", HttpStatus.BAD_REQUEST));

        AiJob job = aiJobService.createJob(currentUser, AiJobType.GITHUB_SYNC, currentUser.getId());
        return AiJobMapper.toDto(job);
    }

    @Transactional
    public void performSync(AiJob job) {
        UUID userId = job.getTargetId();
        GitHubAccount account = gitHubAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("GitHub account is no longer connected.", HttpStatus.NOT_FOUND));

        List<GitHubApiClient.GitHubRepoPayload> repos = gitHubApiClient.fetchRepos(account.getAccessToken());

        for (GitHubApiClient.GitHubRepoPayload payload : repos) {
            GitHubRepo repo = gitHubRepoRepository
                    .findByGithubAccountIdAndGithubRepoId(account.getId(), payload.id())
                    .orElseGet(() -> GitHubRepo.builder().githubAccount(account).githubRepoId(payload.id()).build());

            repo.setName(payload.name());
            repo.setFullName(payload.fullName());
            repo.setDescription(payload.description());
            repo.setLanguage(payload.language());
            repo.setStars(payload.stargazersCount() != null ? payload.stargazersCount() : 0);
            repo.setForks(payload.forksCount() != null ? payload.forksCount() : 0);
            repo.setHtmlUrl(payload.htmlUrl());
            repo.setPrivate(payload.isPrivate() != null && payload.isPrivate());
            repo.setPushedAt(payload.pushedAt());

            gitHubRepoRepository.save(repo);
        }

        account.setLastSyncedAt(Instant.now());
        gitHubAccountRepository.save(account);
        log.info("GitHub sync completed for account {} ({} repos)", account.getId(), repos.size());
    }
}
