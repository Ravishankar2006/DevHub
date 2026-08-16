package com.devhub.ai.dto;

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
public class AIProposedActionDto {
    private UUID id;
    private String actionType;
    private String summary;
    private boolean destructive;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
}
