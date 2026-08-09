package com.devhub.documents.dto;

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
public class DocumentDto {
    private UUID id;
    private String title;
    private String fileName;
    private String status;
    private Instant indexedAt;
    private String errorMessage;
    private Instant createdAt;
}
