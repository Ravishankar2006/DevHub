package com.devhub.jobs.dto;

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
public class AiJobDto {
    private UUID id;
    private String jobType;
    private String status;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
