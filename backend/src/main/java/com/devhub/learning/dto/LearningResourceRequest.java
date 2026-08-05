package com.devhub.learning.dto;

import com.devhub.learning.LearningResourceType;
import com.devhub.learning.LearningStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LearningResourceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private LearningResourceType resourceType;
    private String provider;
    private String url;
    private LearningStatus status;
    private Integer progressPercent;
    private LocalDate estimatedCompletionDate;
    private List<String> tags;
    private String notes;
}
