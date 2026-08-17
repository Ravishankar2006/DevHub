package com.devhub.activity.dto;

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
public class ActivityLogDto {
    private UUID id;
    private String source;
    private String actionType;
    private String targetEntityRef;
    private String summary;
    private Instant createdAt;
}
