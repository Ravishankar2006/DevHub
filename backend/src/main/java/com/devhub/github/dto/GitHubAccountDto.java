package com.devhub.github.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubAccountDto {
    private boolean connected;
    private String username;
    private String avatarUrl;
    private Instant connectedAt;
    private Instant lastSyncedAt;
    private List<GitHubRepoDto> repos;
    private Map<String, Long> languageBreakdown;
}
