package com.devhub.resumes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeMetadataRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String label;
    private String notes;
}
