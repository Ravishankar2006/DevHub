package com.devhub.notes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteFolderRequest {

    @NotBlank(message = "Name is required")
    private String name;
}
