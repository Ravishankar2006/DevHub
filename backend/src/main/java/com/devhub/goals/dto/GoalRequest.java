package com.devhub.goals.dto;

import com.devhub.goals.GoalStatus;
import com.devhub.goals.GoalType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class GoalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private GoalType type;
    private GoalStatus status;
    private LocalDate targetDate;
    private Integer progressPercent;
    private UUID projectId;
}
