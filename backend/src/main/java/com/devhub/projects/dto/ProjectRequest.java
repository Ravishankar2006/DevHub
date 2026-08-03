package com.devhub.projects.dto;

import com.devhub.projects.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String repoUrl;
    private String liveUrl;
    private String roadmap;
    private List<String> stackTags;
    private ProjectStatus status;
    private boolean archived;
}
