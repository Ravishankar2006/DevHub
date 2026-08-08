package com.devhub.brief.dto;

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
public class DailyBriefDto {
    private UUID id;
    private LocalDate briefDate;
    private String content;
    private Instant generatedAt;
}
