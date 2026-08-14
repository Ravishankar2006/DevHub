package com.devhub.github;

import com.devhub.common.ApiException;
import com.devhub.common.StreakCalculator;
import com.devhub.jobs.AiJob;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.jobs.dto.AiJobMapper;
import com.devhub.users.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSyncService {

    private static final int LANGUAGE_FETCH_BUDGET = 30;
    private static final int ACTIVITY_HISTORY_DAYS = 60;

    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final GitHubApiClient gitHubApiClient;
    private final AiJobService aiJobService;
    private final ObjectMapper objectMapper;

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

        // Repos arrive sorted by pushed-desc; bound language-byte lookups to the most recently
        // active repos so a large account doesn't burn its rate limit on stale/archived work.
        int languageCallsRemaining = LANGUAGE_FETCH_BUDGET;
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
            repo.setFork(payload.fork() != null && payload.fork());
            repo.setOpenIssues(payload.openIssuesCount() != null ? payload.openIssuesCount() : 0);
            repo.setWatchers(payload.watchersCount() != null ? payload.watchersCount() : 0);
            repo.setPushedAt(payload.pushedAt());

            if (!repo.isFork() && languageCallsRemaining > 0) {
                languageCallsRemaining--;
                Map<String, Integer> languages = gitHubApiClient.fetchLanguages(account.getAccessToken(), payload.fullName());
                if (!languages.isEmpty()) {
                    repo.setLanguagesJson(writeJson(languages));
                }
            }

            gitHubRepoRepository.save(repo);
        }

        syncActivity(account);

        account.setLastSyncedAt(Instant.now());
        gitHubAccountRepository.save(account);
        log.info("GitHub sync completed for account {} ({} repos)", account.getId(), repos.size());
    }

    private void syncActivity(GitHubAccount account) {
        List<GitHubApiClient.GitHubEventPayload> events =
                gitHubApiClient.fetchEvents(account.getAccessToken(), account.getGithubUsername());

        Map<LocalDate, Integer> dailyCommits = new TreeMap<>();
        for (GitHubApiClient.GitHubEventPayload event : events) {
            if (!"PushEvent".equals(event.type()) || event.createdAt() == null) continue;
            // GitHub's events payload no longer includes distinct_size (it used to), so a push
            // with an unknown commit count still counts as at least one commit for that day --
            // otherwise every push gets silently dropped and streaks/activity read as all-zero.
            Integer distinctSize = event.payload() != null ? event.payload().distinctSize() : null;
            int commits = (distinctSize != null && distinctSize > 0) ? distinctSize : 1;
            LocalDate day = event.createdAt().atZone(ZoneId.systemDefault()).toLocalDate();
            dailyCommits.merge(day, commits, Integer::sum);
        }

        Set<Long> buckets = dailyCommits.keySet().stream().map(LocalDate::toEpochDay).collect(Collectors.toSet());
        LocalDate cutoff30 = LocalDate.now().minusDays(29);
        int commitsLast30Days = dailyCommits.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(cutoff30))
                .mapToInt(Map.Entry::getValue)
                .sum();

        LocalDate historyCutoff = LocalDate.now().minusDays(ACTIVITY_HISTORY_DAYS - 1);
        Map<String, Integer> recentActivity = dailyCommits.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(historyCutoff))
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue, (a, b) -> a, TreeMap::new));

        account.setCurrentStreak((int) StreakCalculator.computeCurrentStreak(buckets));
        account.setLongestStreak((int) StreakCalculator.computeLongestStreak(buckets));
        account.setCommitsLast30Days(commitsLast30Days);
        account.setDailyActivityJson(writeJson(recentActivity));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
