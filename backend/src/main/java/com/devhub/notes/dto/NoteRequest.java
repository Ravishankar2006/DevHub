package com.devhub.notes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class NoteRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String content;
    private List<String> tags;
    private UUID folderId;
}
