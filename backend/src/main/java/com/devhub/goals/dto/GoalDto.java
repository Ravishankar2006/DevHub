package com.devhub.goals.dto;

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
public class GoalDto {
    private UUID id;
    private String title;
    private String description;
    private String type;
    private String status;
    private LocalDate targetDate;
    private int progressPercent;
    private UUID projectId;
    private String projectName;
    private Instant createdAt;
    private Instant updatedAt;
}
