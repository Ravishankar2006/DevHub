package com.devhub.github.dto;

import com.devhub.github.GitHubAccount;
import com.devhub.github.GitHubRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitHubMapper {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int DASHBOARD_ACTIVITY_DAYS = 30;

    public static GitHubRepoDto toDto(GitHubRepo repo) {
        return GitHubRepoDto.builder()
                .id(repo.getId())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .description(repo.getDescription())
                .language(repo.getLanguage())
                .stars(repo.getStars())
                .forks(repo.getForks())
                .htmlUrl(repo.getHtmlUrl())
                .isPrivate(repo.isPrivate())
                .isFork(repo.isFork())
                .openIssues(repo.getOpenIssues())
                .watchers(repo.getWatchers())
                .pushedAt(repo.getPushedAt())
                .build();
    }

    public static GitHubAccountDto toDto(GitHubAccount account, List<GitHubRepo> repos) {
        int totalStars = repos.stream().mapToInt(GitHubRepo::getStars).sum();
        int originalRepoCount = (int) repos.stream().filter(r -> !r.isFork()).count();

        return GitHubAccountDto.builder()
                .connected(true)
                .username(account.getGithubUsername())
                .avatarUrl(account.getAvatarUrl())
                .connectedAt(account.getConnectedAt())
                .lastSyncedAt(account.getLastSyncedAt())
                .repos(repos.stream().map(GitHubMapper::toDto).toList())
                .languageBreakdown(aggregateLanguages(repos))
                .totalStars(totalStars)
                .originalRepoCount(originalRepoCount)
                .forkedRepoCount(repos.size() - originalRepoCount)
                .currentStreak(account.getCurrentStreak())
                .longestStreak(account.getLongestStreak())
                .commitsLast30Days(account.getCommitsLast30Days())
                .dailyActivity(recentActivity(account.getDailyActivityJson()))
                .build();
    }

    public static GitHubAccountDto disconnected() {
        return GitHubAccountDto.builder()
                .connected(false)
                .repos(List.of())
                .languageBreakdown(Map.of())
                .dailyActivity(Map.of())
                .build();
    }

    /** Byte-weighted across each repo's actual language mix; falls back to a per-repo count for repos synced before this existed. */
    private static Map<String, Long> aggregateLanguages(List<GitHubRepo> repos) {
        Map<String, Long> totals = new HashMap<>();
        for (GitHubRepo repo : repos) {
            if (repo.isFork() || repo.getLanguagesJson() == null) continue;
            try {
                Map<String, Integer> bytes = JSON.readValue(repo.getLanguagesJson(), new TypeReference<Map<String, Integer>>() {});
                bytes.forEach((lang, count) -> totals.merge(lang, count.longValue(), Long::sum));
            } catch (Exception ignored) {
                // repo simply doesn't contribute to the breakdown
            }
        }
        if (!totals.isEmpty()) return totals;

        return repos.stream()
                .filter(r -> r.getLanguage() != null)
                .collect(Collectors.groupingBy(GitHubRepo::getLanguage, Collectors.counting()));
    }

    private static Map<String, Integer> recentActivity(String dailyActivityJson) {
        Map<String, Integer> parsed;
        try {
            parsed = dailyActivityJson == null ? Map.of()
                    : JSON.readValue(dailyActivityJson, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            parsed = Map.of();
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(DASHBOARD_ACTIVITY_DAYS - 1);
        for (int i = 0; i < DASHBOARD_ACTIVITY_DAYS; i++) {
            String key = start.plusDays(i).toString();
            result.put(key, parsed.getOrDefault(key, 0));
        }
        return result;
    }
}
