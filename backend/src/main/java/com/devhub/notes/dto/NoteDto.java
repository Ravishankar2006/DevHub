package com.devhub.notes.dto;

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
public class NoteDto {
    private UUID id;
    private UUID folderId;
    private String folderName;
    private String title;
    private String content;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
