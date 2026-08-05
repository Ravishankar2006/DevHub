package com.devhub.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResourceDto {
    private UUID id;
    private String title;
    private String resourceType;
    private String provider;
    private String url;
    private String status;
    private int progressPercent;
    private LocalDate estimatedCompletionDate;
    private List<String> tags;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
