package com.devhub.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneDto {
    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private long totalTasks;
    private long completedTasks;
    private int progressPercent;
    private Instant createdAt;
    private Instant updatedAt;
}
