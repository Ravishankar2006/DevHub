package com.devhub.github.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepoDto {
    private UUID id;
    private String name;
    private String fullName;
    private String description;
    private String language;
    private int stars;
    private int forks;
    private String htmlUrl;
    private boolean isPrivate;
    private Instant pushedAt;
}
