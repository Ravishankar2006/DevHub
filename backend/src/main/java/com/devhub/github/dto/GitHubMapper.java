package com.devhub.github.dto;

import com.devhub.github.GitHubAccount;
import com.devhub.github.GitHubRepo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitHubMapper {

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
                .pushedAt(repo.getPushedAt())
                .build();
    }

    public static GitHubAccountDto toDto(GitHubAccount account, List<GitHubRepo> repos) {
        Map<String, Long> languageBreakdown = repos.stream()
                .filter(r -> r.getLanguage() != null)
                .collect(Collectors.groupingBy(GitHubRepo::getLanguage, Collectors.counting()));

        return GitHubAccountDto.builder()
                .connected(true)
                .username(account.getGithubUsername())
                .avatarUrl(account.getAvatarUrl())
                .connectedAt(account.getConnectedAt())
                .lastSyncedAt(account.getLastSyncedAt())
                .repos(repos.stream().map(GitHubMapper::toDto).toList())
                .languageBreakdown(languageBreakdown)
                .build();
    }

    public static GitHubAccountDto disconnected() {
        return GitHubAccountDto.builder()
                .connected(false)
                .repos(List.of())
                .languageBreakdown(Map.of())
                .build();
    }
}
