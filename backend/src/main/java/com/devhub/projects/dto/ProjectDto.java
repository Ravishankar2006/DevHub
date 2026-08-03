package com.devhub.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private UUID id;
    private String name;
    private String description;
    private String repoUrl;
    private String liveUrl;
    private String roadmap;
    private List<String> stackTags;
    private String status;
    private boolean archived;
    private long taskCount;
    private long completedTaskCount;
    private long milestoneCount;
    private Instant createdAt;
    private Instant updatedAt;
}
