package com.devhub.resumes.dto;

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
public class ResumeDto {
    private UUID id;
    private String name;
    private String label;
    private String fileName;
    private long fileSizeBytes;
    private String notes;
    private String downloadUrl;
    private Integer reviewScore;
    private String reviewSummary;
    private List<String> reviewIssues;
    private List<String> reviewSuggestions;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
