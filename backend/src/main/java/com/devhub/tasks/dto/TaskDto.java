package com.devhub.tasks.dto;

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
public class TaskDto {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private UUID milestoneId;
    private String milestoneTitle;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private int position;
    private Instant createdAt;
    private Instant updatedAt;
}
