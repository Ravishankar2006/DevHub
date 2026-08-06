package com.devhub.resumes.dto;

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
public class ResumeDto {
    private UUID id;
    private String name;
    private String label;
    private String fileName;
    private long fileSizeBytes;
    private String notes;
    private String downloadUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
