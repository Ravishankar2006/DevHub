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
public class AIConversationSummaryDto {
    private UUID id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
}
